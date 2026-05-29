package es.noa.rad.map.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileMaterial;
import es.noa.rad.map.TileShape;

/**
 * Loads a {@link TileMap} from a JSON file on the classpath.
 *
 * <p>Minimal hand-rolled JSON parser (no external deps) for the project map
 * format:</p>
 *
 * <pre>{@code
 * {
 *   "width":  5,
 *   "depth":  5,
 *   "length": 1,                  // number of Z levels
 *   "tileMap": [                  // palette: index -> (material, shape)
 *     { "tile": { "index": 0, "material": "TileMaterial.GRASS",
 *                 "Shape":    "TileShape.FLOOR" } },
 *     ...
 *   ],
 *   "tileLayer": [ 0,0,0,...,1 ]  // flat row-major indices, size = width*depth*length
 * }
 * }</pre>
 *
 * <p>The {@code tileLayer} array is read in row-major order per Z level
 * (level 0 first = ground level, then ascending Z). When {@code length == 1}
 * the array is simply {@code width * depth} entries.</p>
 *
 * <p>Material and shape names may be written with or without the
 * {@code TileMaterial.} / {@code TileShape.} prefix.</p>
 *
 * @since Phase 3 debt-close
 */
public final class MapLoader {

    /** Palette entry: material + shape paired by an int id. */
    private record PaletteEntry(TileMaterial material, TileShape shape) {}

    private MapLoader() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Loads a map from a classpath resource (e.g. {@code "map/map_1.json"}).
     *
     * @param classpathResource path relative to {@code src/main/resources}
     * @return the parsed {@link TileMap}
     * @throws IOException if the resource cannot be read
     * @throws IllegalArgumentException if the JSON is malformed
     */
    public static TileMap loadFromClasspath(final String classpathResource) throws IOException {
        try (InputStream in = MapLoader.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IOException("Map resource not found on classpath: " + classpathResource);
            }
            final String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return parse(json);
        }
    }

    /**
     * Parses a JSON string directly.
     *
     * @param json the full JSON document
     * @return the parsed {@link TileMap}
     */
    public static TileMap parse(final String json) {
        final String stripped = stripComments(json);

        final int width  = readIntField(stripped, "width");
        final int depth  = readIntField(stripped, "depth");
        final int length = readIntField(stripped, "length");

        final Map<Integer, PaletteEntry> palette = parsePalette(stripped);
        final int[] indices = parseTileLayerIndices(stripped);

        final int expected = width * depth * length;
        if (indices.length != expected) {
            throw new IllegalArgumentException(
                "tileLayer size " + indices.length + " does not match width*depth*length = " + expected);
        }

        final TileLayer layer = new TileLayer(width, depth);
        int p = 0;
        for (int z = 0; z < length; z++) {
            for (int row = 0; row < depth; row++) {
                for (int col = 0; col < width; col++) {
                    final int idx = indices[p++];
                    final PaletteEntry pe = palette.get(idx);
                    if (pe == null) continue;   // unmapped index = empty cell
                    layer.setTile(col, row, z, pe.material(), pe.shape());
                }
            }
        }
        return new TileMap(layer);
    }

    /**
     * Convenience: center cell of a map.
     *
     * @param map source map
     * @return integer array {col, row} pointing to the central cell
     */
    public static int[] centerCell(final TileMap map) {
        return new int[] { map.getWidth() / 2, map.getDepth() / 2 };
    }

    /**
     * Convenience: returns the surface Z immediately above the top-most tile
     * at {@code (col, row)}, so an entity can be placed standing on it.
     *
     * @param map source map
     * @param col cell column
     * @param row cell row
     * @return surface elevation in tile units, or 0 if the cell is empty
     */
    public static float surfaceElevation(final TileMap map, final int col, final int row) {
        final Tile top = map.getTopTile(col, row);
        if (top == null) return 0f;
        // Use the average of the 4 absolute corner heights so the player
        // stands neatly on flat / ramp surfaces alike.
        final float nw = top.absoluteCornerHeight(TileShape.Corner.NW);
        final float ne = top.absoluteCornerHeight(TileShape.Corner.NE);
        final float se = top.absoluteCornerHeight(TileShape.Corner.SE);
        final float sw = top.absoluteCornerHeight(TileShape.Corner.SW);
        return (nw + ne + se + sw) * 0.25f;
    }

    // -------------------------------------------------------------------------
    // JSON parsing helpers (intentionally tiny; only what this format needs)
    // -------------------------------------------------------------------------

    private static String stripComments(final String src) {
        // Remove // line comments and /* block */ comments to be lenient.
        String s = src.replaceAll("(?m)//.*$", "");
        s = s.replaceAll("(?s)/\\*.*?\\*/", "");
        return s;
    }

    private static int readIntField(final String json, final String key) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)")
            .matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing numeric field: " + key);
        }
        return Integer.parseInt(m.group(1));
    }

    private static String readStringField(final String objectBody, final String key) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"",
                     Pattern.CASE_INSENSITIVE)
            .matcher(objectBody);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing string field: " + key);
        }
        return m.group(1).trim();
    }

    /**
     * Parses the {@code tileLayer} value, supporting both formats:
     * <ul>
     *   <li>Flat array (legacy): {@code "tileLayer": [0,1,0,...]}</li>
     *   <li>Layered array (new): {@code "tileLayer": [{"data":[...]},{"data":[...]}]}</li>
     * </ul>
     * In the layered format each object corresponds to one Z level in order.
     */
    private static int[] parseTileLayerIndices(final String json) {
        // Find the outermost [ ... ] for the tileLayer key.
        final Matcher start = Pattern
            .compile("\"tileLayer\"\\s*:\\s*\\[")
            .matcher(json);
        if (!start.find()) {
            throw new IllegalArgumentException("Missing array field: tileLayer");
        }
        // Walk forward to find the matching closing bracket (handles nesting).
        int depth = 1;
        int i = start.end();
        final StringBuilder body = new StringBuilder();
        while (i < json.length() && depth > 0) {
            final char c = json.charAt(i);
            if      (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) break; }
            body.append(c);
            i++;
        }
        final String content = body.toString().trim();

        // Detect format: if first non-whitespace char is '{' it is the layered format.
        if (content.startsWith("{")) {
            // Layered format – extract each "data": [ ... ] sub-array and concatenate.
            final List<Integer> all = new ArrayList<>();
            final Pattern dataPat = Pattern.compile("\"data\"\\s*:\\s*\\[([^\\]]*)\\]");
            final Matcher dm = dataPat.matcher(content);
            while (dm.find()) {
                collectInts(dm.group(1), all);
            }
            if (all.isEmpty()) {
                throw new IllegalArgumentException("tileLayer layered format contains no data arrays");
            }
            return all.stream().mapToInt(Integer::intValue).toArray();
        } else {
            // Flat format.
            final List<Integer> all = new ArrayList<>();
            collectInts(content, all);
            return all.stream().mapToInt(Integer::intValue).toArray();
        }
    }

    private static void collectInts(final String csv, final List<Integer> out) {
        if (csv == null || csv.isBlank()) return;
        for (final String p : csv.split("[,\\s]+")) {
            if (!p.isBlank()) out.add(Integer.parseInt(p.trim()));
        }
    }

    private static int[] parseIntArray(final String json, final String key) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[([^\\]]*)\\]")
            .matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing array field: " + key);
        }
        final String body = m.group(1).trim();
        if (body.isEmpty()) return new int[0];
        final String[] parts = body.split("[,\\s]+");
        final List<Integer> tmp = new ArrayList<>(parts.length);
        for (final String p : parts) {
            if (!p.isBlank()) tmp.add(Integer.parseInt(p.trim()));
        }
        final int[] out = new int[tmp.size()];
        for (int i = 0; i < out.length; i++) out[i] = tmp.get(i);
        return out;
    }

    /**
     * Parses the {@code tileMap} array, returning a palette keyed by the
     * {@code index} field of each entry.
     */
    private static Map<Integer, PaletteEntry> parsePalette(final String json) {
        // Locate the tileMap array first.
        final Matcher arr = Pattern
            .compile("\"tileMap\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL)
            .matcher(json);
        if (!arr.find()) {
            throw new IllegalArgumentException("Missing array field: tileMap");
        }
        final String arrayBody = arr.group(1);

        // Each entry is {"tile":{...}}. Match each inner tile object.
        final Pattern tilePat = Pattern.compile(
            "\"tile\"\\s*:\\s*\\{([^}]*)\\}", Pattern.DOTALL);
        final Matcher m = tilePat.matcher(arrayBody);

        final Map<Integer, PaletteEntry> palette = new HashMap<>();
        while (m.find()) {
            final String body  = m.group(1);
            final int    index = Integer.parseInt(readStringOrInt(body, "index"));
            // Accept both "material" and "shape"/"Shape" case-insensitively.
            final String mat   = readStringField(body, "material");
            final String shape = readStringFieldFlexible(body, "shape");
            palette.put(index, new PaletteEntry(parseMaterial(mat), parseShape(shape)));
        }
        if (palette.isEmpty()) {
            throw new IllegalArgumentException("tileMap palette is empty");
        }
        return palette;
    }

    /** Reads a number that may appear quoted or unquoted. */
    private static String readStringOrInt(final String body, final String key) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?(-?\\d+)\"?")
            .matcher(body);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return m.group(1);
    }

    /** Case-insensitive lookup for the shape key (json has "Shape"). */
    private static String readStringFieldFlexible(final String body, final String key) {
        final Matcher m = Pattern
            .compile("\"(?i:" + Pattern.quote(key) + ")\"\\s*:\\s*\"([^\"]*)\"")
            .matcher(body);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing string field: " + key);
        }
        return m.group(1).trim();
    }

    private static TileMaterial parseMaterial(final String raw) {
        final String name = stripPrefix(raw, "TileMaterial.");
        try {
            return TileMaterial.valueOf(name);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown TileMaterial: " + raw);
        }
    }

    private static TileShape parseShape(final String raw) {
        final String name = stripPrefix(raw, "TileShape.");
        try {
            return TileShape.valueOf(name);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown TileShape: " + raw);
        }
    }

    private static String stripPrefix(final String raw, final String prefix) {
        final String trimmed = raw.trim();
        return trimmed.startsWith(prefix) ? trimmed.substring(prefix.length()) : trimmed;
    }
}
