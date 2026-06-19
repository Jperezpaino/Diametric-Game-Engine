package es.noa.rad.map.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.noa.rad.map.MapResource;
import es.noa.rad.map.StructureInstance;
import es.noa.rad.map.TileInstance;

/**
 * Reads / writes the <b>v2 map document</b> (Phase 9d) into and out of
 * {@link MapResource}.
 *
 * <p>On-disk shape:</p>
 * <pre>{@code
 * {
 *   "formatVersion": 2,
 *   "width":  15,
 *   "depth":  15,
 *   "length":  3,
 *   "tilePalette": [
 *     { "index": 0, "tileId": "grass_floor" },
 *     { "index": 1, "tileId": "stone_block" }
 *   ],
 *   "tileLayer": [
 *     { "data": [ 0, 0, -1, ... ] },
 *     { "data": [ -1, 1, ... ] }
 *   ],
 *   "structureInstances": [
 *     { "structureId": "small_pillar", "x": 5, "y": 5, "z": 0 }
 *   ]
 * }
 * }</pre>
 *
 * <p>The dense palette + indices layout is identical in spirit to v1 so
 * large grids stay compact, but palette entries reference
 * {@code TileDefinition} ids (string), not the legacy
 * {@code (TileMaterial, TileShape)} enum pair. Empty cells are encoded as
 * {@code -1}. The {@code structureInstances} array is emitted even when
 * empty (consistent with the rest of the {@code es.noa.rad.map.io}
 * writers).</p>
 *
 * <p>Loading the legacy v1 format (palette of {@code "TileMaterial.X"} +
 * {@code "TileShape.Y"}) is handled by {@link MapLoader} for runtime
 * back-compat; this class is strictly v2.</p>
 *
 * @since Phase 9d
 */
public final class MapResourceIO {

    /** Current on-disk format version emitted by {@link #toJson(MapResource)}. */
    public static final int FORMAT_VERSION = 2;

    private MapResourceIO() {}

    // -------------------------------------------------------------------------
    // Reading
    // -------------------------------------------------------------------------

    /** Loads a map resource from a classpath resource (e.g. {@code "map/level_01.json"}). */
    public static MapResource loadFromClasspath(final String classpathResource) throws IOException {
        try (InputStream in = MapResourceIO.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IOException("Map resource not found on classpath: " + classpathResource);
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** Loads a map resource from a filesystem path. */
    public static MapResource loadFromFile(final Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8));
    }

    /** Parses a v2 JSON map document into a {@link MapResource}. */
    public static MapResource parse(final String json) {
        final String stripped = Json.stripComments(json);
        final int version = Json.readIntField(stripped, "formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported MapResource formatVersion: " + version + " (expected " + FORMAT_VERSION + ")");
        }

        final int width  = Json.readIntField(stripped, "width");
        final int depth  = Json.readIntField(stripped, "depth");
        final int length = Json.readIntField(stripped, "length");

        final Map<Integer, String> palette = parsePalette(stripped);
        final int[] indices = parseTileLayerIndices(stripped);

        final int expected = width * depth * length;
        if (indices.length != expected) {
            throw new IllegalArgumentException(
                "tileLayer size " + indices.length + " does not match width*depth*length = " + expected);
        }

        final List<TileInstance> tiles = new ArrayList<>();
        int p = 0;
        for (int z = 0; z < length; z++) {
            for (int row = 0; row < depth; row++) {
                for (int col = 0; col < width; col++) {
                    final int idx = indices[p++];
                    if (idx < 0) continue;
                    final String tileId = palette.get(idx);
                    if (tileId == null) {
                        throw new IllegalArgumentException(
                            "tileLayer references missing palette index: " + idx);
                    }
                    tiles.add(new TileInstance(tileId, col, row, z));
                }
            }
        }

        final List<StructureInstance> structures = parseStructureInstances(stripped);
        return new MapResource(width, depth, length, tiles, structures);
    }

    // -------------------------------------------------------------------------
    // Writing
    // -------------------------------------------------------------------------

    /** Saves {@code map} as JSON to {@code destination} (UTF-8, overwrites). */
    public static void saveToFile(final MapResource map, final Path destination) throws IOException {
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.writeString(destination, toJson(map), StandardCharsets.UTF_8);
    }

    /** Serialises {@code map} to a pretty-printed v2 JSON document. */
    public static String toJson(final MapResource map) {
        final int width  = map.width();
        final int depth  = map.depth();
        final int length = map.length();

        final List<String>            paletteOrder = new ArrayList<>();
        final Map<String, Integer>    paletteIndex = new HashMap<>();
        final int[][][]               grid         = new int[length][depth][width];
        for (int[][] plane : grid) for (int[] row : plane) java.util.Arrays.fill(row, -1);

        for (final TileInstance ti : map.tiles()) {
            checkBounds(ti, width, depth, length);
            final Integer existing = paletteIndex.get(ti.tileId());
            final int idx;
            if (existing == null) {
                idx = paletteOrder.size();
                paletteOrder.add(ti.tileId());
                paletteIndex.put(ti.tileId(), idx);
            } else {
                idx = existing;
            }
            grid[ti.z()][ti.y()][ti.x()] = idx;
        }

        final StringBuilder sb = new StringBuilder(8192);
        sb.append("{\n");
        sb.append("  \"formatVersion\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"width\":  ").append(width) .append(",\n");
        sb.append("  \"depth\":  ").append(depth) .append(",\n");
        sb.append("  \"length\": ").append(length).append(",\n");
        appendPalette(sb, paletteOrder);
        sb.append(",\n");
        appendTileLayer(sb, grid, width, depth, length);
        sb.append(",\n");
        appendStructureInstances(sb, map.structures());
        sb.append("\n}\n");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Internals - parsing
    // -------------------------------------------------------------------------

    private static Map<Integer, String> parsePalette(final String json) {
        final String body = Json.extractArrayBody(json, "tilePalette");
        if (body == null) {
            throw new IllegalArgumentException("Missing array field: tilePalette");
        }
        final Map<Integer, String> palette = new HashMap<>();
        for (final String entryBody : Json.splitObjects(body)) {
            final int    index  = Json.readIntField   (entryBody, "index");
            final String tileId = Json.readStringField(entryBody, "tileId");
            palette.put(index, tileId);
        }
        if (palette.isEmpty()) {
            throw new IllegalArgumentException("tilePalette is empty");
        }
        return palette;
    }

    private static int[] parseTileLayerIndices(final String json) {
        final String body = Json.extractArrayBody(json, "tileLayer");
        if (body == null) {
            throw new IllegalArgumentException("Missing array field: tileLayer");
        }
        // Each layer is an object with a "data" array. Concatenate them in order.
        final List<Integer> all = new ArrayList<>();
        final Pattern dataPat = Pattern.compile("\"data\"\\s*:\\s*\\[([^\\]]*)\\]");
        final Matcher dm = dataPat.matcher(body);
        while (dm.find()) {
            collectInts(dm.group(1), all);
        }
        if (all.isEmpty()) {
            throw new IllegalArgumentException("tileLayer contains no data arrays");
        }
        final int[] out = new int[all.size()];
        for (int i = 0; i < out.length; i++) out[i] = all.get(i);
        return out;
    }

    private static List<StructureInstance> parseStructureInstances(final String json) {
        final String body = Json.extractArrayBody(json, "structureInstances");
        if (body == null) return List.of();
        final List<StructureInstance> out = new ArrayList<>();
        for (final String entryBody : Json.splitObjects(body)) {
            final String structureId = Json.readStringField(entryBody, "structureId");
            final int x = Json.readIntField(entryBody, "x");
            final int y = Json.readIntField(entryBody, "y");
            final int z = Json.readIntField(entryBody, "z");
            out.add(new StructureInstance(structureId, x, y, z));
        }
        return out;
    }

    private static void collectInts(final String csv, final List<Integer> out) {
        if (csv == null || csv.isBlank()) return;
        for (final String p : csv.split("[,\\s]+")) {
            if (!p.isBlank()) out.add(Integer.parseInt(p.trim()));
        }
    }

    // -------------------------------------------------------------------------
    // Internals - writing
    // -------------------------------------------------------------------------

    private static void checkBounds(final TileInstance ti,
                                     final int width, final int depth, final int length) {
        if (ti.x() < 0 || ti.x() >= width
         || ti.y() < 0 || ti.y() >= depth
         || ti.z() < 0 || ti.z() >= length) {
            throw new IllegalArgumentException(
                "TileInstance(" + ti.tileId() + "," + ti.x() + "," + ti.y() + "," + ti.z()
                    + ") is outside the map extent " + width + "x" + depth + "x" + length);
        }
    }

    private static void appendPalette(final StringBuilder sb, final List<String> palette) {
        sb.append("  \"tilePalette\": [");
        if (palette.isEmpty()) {
            sb.append(']');
            return;
        }
        sb.append('\n');
        for (int i = 0; i < palette.size(); i++) {
            sb.append("    { \"index\": ").append(i)
              .append(", \"tileId\": \"").append(Json.escape(palette.get(i))).append("\" }");
            if (i < palette.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]");
    }

    private static void appendTileLayer(final StringBuilder sb, final int[][][] grid,
                                         final int width, final int depth, final int length) {
        sb.append("  \"tileLayer\": [\n");
        for (int z = 0; z < length; z++) {
            sb.append("    {\n");
            sb.append("      \"data\": [\n");
            for (int row = 0; row < depth; row++) {
                sb.append("        ");
                for (int col = 0; col < width; col++) {
                    sb.append(String.format("%2d", grid[z][row][col]));
                    final boolean lastCell = (col == width - 1) && (row == depth - 1);
                    if (!lastCell) sb.append(',');
                    if (col < width - 1) sb.append(' ');
                }
                sb.append('\n');
            }
            sb.append("      ]\n");
            sb.append("    }");
            if (z < length - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]");
    }

    private static void appendStructureInstances(final StringBuilder sb,
                                                  final List<StructureInstance> structures) {
        sb.append("  \"structureInstances\": [");
        if (structures.isEmpty()) {
            sb.append(']');
            return;
        }
        sb.append('\n');
        for (int i = 0; i < structures.size(); i++) {
            final StructureInstance s = structures.get(i);
            sb.append("    { \"structureId\": \"").append(Json.escape(s.structureId()))
              .append("\", \"x\": ").append(s.x())
              .append(", \"y\": ") .append(s.y())
              .append(", \"z\": ") .append(s.z())
              .append(" }");
            if (i < structures.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]");
    }
}
