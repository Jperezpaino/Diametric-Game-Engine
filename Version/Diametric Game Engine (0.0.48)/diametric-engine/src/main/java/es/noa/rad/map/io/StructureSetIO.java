package es.noa.rad.map.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.noa.rad.map.Structure;
import es.noa.rad.map.TileInstance;

/**
 * Reads / writes <em>structure sets</em> &mdash; collections of
 * {@link Structure} resources serialised as JSON with an explicit
 * {@code formatVersion}.
 *
 * <p>On-disk shape:</p>
 * <pre>{@code
 * {
 *   "formatVersion": 1,
 *   "structures": [
 *     {
 *       "id":   "small_pillar",
 *       "name": "Small Pillar",
 *       "tiles": [
 *         { "tileId": "stone_block", "x": 0, "y": 0, "z": 0 },
 *         { "tileId": "stone_block", "x": 0, "y": 0, "z": 1 }
 *       ]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <p>Tile coordinates inside a structure are <b>relative</b> to its origin
 * at {@code (0, 0, 0)}; the absolute placement is supplied by a
 * {@link es.noa.rad.map.StructureInstance StructureInstance} inside a map
 * (Phase 9c).</p>
 *
 * @since Phase 9b
 */
public final class StructureSetIO {

    /** Current on-disk format version emitted by {@link #toJson(List)}. */
    public static final int FORMAT_VERSION = 1;

    private StructureSetIO() {}

    // -------------------------------------------------------------------------
    // Reading
    // -------------------------------------------------------------------------

    /** Loads a structure set from a classpath resource (e.g. {@code "sets/structures.json"}). */
    public static List<Structure> loadFromClasspath(final String classpathResource) throws IOException {
        try (InputStream in = StructureSetIO.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IOException("Structure set resource not found on classpath: " + classpathResource);
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** Loads a structure set from a filesystem path. */
    public static List<Structure> loadFromFile(final Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8));
    }

    /** Parses a JSON structure set document. */
    public static List<Structure> parse(final String json) {
        final String stripped = Json.stripComments(json);
        final int version = Json.readIntField(stripped, "formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported StructureSet formatVersion: " + version + " (expected " + FORMAT_VERSION + ")");
        }
        final String arrayBody = Json.extractArrayBody(stripped, "structures");
        if (arrayBody == null) {
            throw new IllegalArgumentException("Missing array field: structures");
        }
        final List<Structure> out = new ArrayList<>();
        for (final String body : Json.splitObjects(arrayBody)) {
            final String id   = Json.readStringField(body, "id");
            final String name = Json.readStringField(body, "name");
            final List<TileInstance> tiles = parseTileInstances(body, id);
            out.add(new Structure(id, name, tiles));
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // Writing
    // -------------------------------------------------------------------------

    /** Saves the structure set to {@code destination} (UTF-8, overwrites). */
    public static void saveToFile(final List<Structure> structures, final Path destination) throws IOException {
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.writeString(destination, toJson(structures), StandardCharsets.UTF_8);
    }

    /** Serialises the structure set to a pretty-printed JSON string. */
    public static String toJson(final List<Structure> structures) {
        final StringBuilder sb = new StringBuilder(512 + 256 * structures.size());
        sb.append("{\n");
        sb.append("  \"formatVersion\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"structures\": [");
        if (structures.isEmpty()) {
            sb.append("]\n");
        } else {
            sb.append('\n');
            for (int i = 0; i < structures.size(); i++) {
                appendStructure(sb, structures.get(i));
                if (i < structures.size() - 1) sb.append(',');
                sb.append('\n');
            }
            sb.append("  ]\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private static List<TileInstance> parseTileInstances(final String structureBody, final String structureId) {
        final String tilesBody = Json.extractArrayBody(structureBody, "tiles");
        if (tilesBody == null) {
            throw new IllegalArgumentException(
                "Structure '" + structureId + "' is missing the required 'tiles' array");
        }
        final List<TileInstance> out = new ArrayList<>();
        for (final String tileBody : Json.splitObjects(tilesBody)) {
            final String tileId = Json.readStringField(tileBody, "tileId");
            final int x = Json.readIntField(tileBody, "x");
            final int y = Json.readIntField(tileBody, "y");
            final int z = Json.readIntField(tileBody, "z");
            out.add(new TileInstance(tileId, x, y, z));
        }
        return out;
    }

    private static void appendStructure(final StringBuilder sb, final Structure structure) {
        sb.append("    {\n");
        sb.append("      \"id\":   \"").append(Json.escape(structure.id())).append("\",\n");
        sb.append("      \"name\": \"").append(Json.escape(structure.name())).append("\",\n");
        sb.append("      \"tiles\": [");
        final List<TileInstance> tiles = structure.tiles();
        if (tiles.isEmpty()) {
            sb.append("]\n");
        } else {
            sb.append('\n');
            for (int j = 0; j < tiles.size(); j++) {
                final TileInstance t = tiles.get(j);
                sb.append("        { \"tileId\": \"").append(Json.escape(t.tileId()))
                  .append("\", \"x\": ").append(t.x())
                  .append(", \"y\": ") .append(t.y())
                  .append(", \"z\": ") .append(t.z())
                  .append(" }");
                if (j < tiles.size() - 1) sb.append(',');
                sb.append('\n');
            }
            sb.append("      ]\n");
        }
        sb.append("    }");
    }
}
