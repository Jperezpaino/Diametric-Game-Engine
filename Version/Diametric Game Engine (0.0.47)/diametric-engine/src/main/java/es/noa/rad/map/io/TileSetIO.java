package es.noa.rad.map.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.noa.rad.map.TileDefinition;
import es.noa.rad.map.TileShape;

/**
 * Reads / writes <em>tile sets</em> &mdash; collections of
 * {@link TileDefinition} resources serialised as JSON with an explicit
 * {@code formatVersion}.
 *
 * <p>On-disk shape:</p>
 * <pre>{@code
 * {
 *   "formatVersion": 1,
 *   "tiles": [
 *     { "id": "grass_floor",   "name": "Grass Floor",
 *       "skinId": "grass_floor", "materialId": "grass", "shape": "FLOOR" },
 *     { "id": "stone_block",   "name": "Stone Block",
 *       "skinId": "stone_block", "materialId": "stone", "shape": "BLOCK" }
 *   ]
 * }
 * }</pre>
 *
 * <p>Tile sets reference skins and materials by <em>id</em> only &mdash; this
 * file does not embed visual or physical detail. Resolving those references
 * is the registry layer's job ({@code SkinRegistry},
 * {@code MaterialRegistry}).</p>
 *
 * @since Phase 8e
 */
public final class TileSetIO {

    /** Current on-disk format version emitted by {@link #toJson(List)}. */
    public static final int FORMAT_VERSION = 1;

    private TileSetIO() {}

    // -------------------------------------------------------------------------
    // Reading
    // -------------------------------------------------------------------------

    /** Loads a tile set from a classpath resource (e.g. {@code "sets/tiles.json"}). */
    public static List<TileDefinition> loadFromClasspath(final String classpathResource) throws IOException {
        try (InputStream in = TileSetIO.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IOException("Tile set resource not found on classpath: " + classpathResource);
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** Loads a tile set from a filesystem path. */
    public static List<TileDefinition> loadFromFile(final Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8));
    }

    /** Parses a JSON tile set document. */
    public static List<TileDefinition> parse(final String json) {
        final String stripped = Json.stripComments(json);
        final int version = Json.readIntField(stripped, "formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported TileSet formatVersion: " + version + " (expected " + FORMAT_VERSION + ")");
        }
        final String arrayBody = Json.extractArrayBody(stripped, "tiles");
        if (arrayBody == null) {
            throw new IllegalArgumentException("Missing array field: tiles");
        }
        final List<TileDefinition> out = new ArrayList<>();
        for (final String body : Json.splitObjects(arrayBody)) {
            final String id         = Json.readStringField(body, "id");
            final String name       = Json.readStringField(body, "name");
            final String skinId     = Json.readStringField(body, "skinId");
            final String materialId = Json.readStringField(body, "materialId");
            final String shapeRaw   = Json.readStringField(body, "shape");
            final TileShape shape;
            try {
                shape = TileShape.valueOf(shapeRaw);
            } catch (final IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown TileShape '" + shapeRaw + "' in tile '" + id + "'");
            }
            out.add(new TileDefinition(id, name, skinId, materialId, shape));
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // Writing
    // -------------------------------------------------------------------------

    /** Saves the tile set to {@code destination} (UTF-8, overwrites). */
    public static void saveToFile(final List<TileDefinition> tiles, final Path destination) throws IOException {
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.writeString(destination, toJson(tiles), StandardCharsets.UTF_8);
    }

    /** Serialises the tile set to a pretty-printed JSON string. */
    public static String toJson(final List<TileDefinition> tiles) {
        final StringBuilder sb = new StringBuilder(512 + 144 * tiles.size());
        sb.append("{\n");
        sb.append("  \"formatVersion\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"tiles\": [\n");
        for (int i = 0; i < tiles.size(); i++) {
            final TileDefinition t = tiles.get(i);
            sb.append("    { \"id\": \"")        .append(Json.escape(t.id()))
              .append("\", \"name\": \"")        .append(Json.escape(t.name()))
              .append("\", \"skinId\": \"")      .append(Json.escape(t.skinId()))
              .append("\", \"materialId\": \"")  .append(Json.escape(t.materialId()))
              .append("\", \"shape\": \"")       .append(t.shape().name())
              .append("\" }");
            if (i < tiles.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }
}
