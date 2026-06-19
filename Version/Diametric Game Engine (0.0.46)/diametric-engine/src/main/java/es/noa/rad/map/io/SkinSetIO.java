package es.noa.rad.map.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.noa.rad.map.Skin;

/**
 * Reads / writes <em>skin sets</em> &mdash; collections of {@link Skin}
 * resources serialised as JSON with an explicit {@code formatVersion}.
 *
 * <p>On-disk shape:</p>
 * <pre>{@code
 * {
 *   "formatVersion": 1,
 *   "skins": [
 *     { "id": "grass_floor", "name": "Grass Floor", "image": "tiles/grass_floor.png" },
 *     { "id": "stone_block", "name": "Stone Block", "image": "tiles/stone_block.png" }
 *   ]
 * }
 * }</pre>
 *
 * <p>This is the dedicated visual catalogue corresponding to clause 6 of the
 * Map System spec. Materials, tile recipes, structures and maps live in
 * their own files (see {@link MaterialSetIO}, {@link TileSetIO}, etc.) so
 * each resource can evolve and be reused independently.</p>
 *
 * @since Phase 8e
 */
public final class SkinSetIO {

    /** Current on-disk format version emitted by {@link #toJson(List)}. */
    public static final int FORMAT_VERSION = 1;

    private SkinSetIO() {}

    // -------------------------------------------------------------------------
    // Reading
    // -------------------------------------------------------------------------

    /** Loads a skin set from a classpath resource (e.g. {@code "sets/skins.json"}). */
    public static List<Skin> loadFromClasspath(final String classpathResource) throws IOException {
        try (InputStream in = SkinSetIO.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IOException("Skin set resource not found on classpath: " + classpathResource);
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** Loads a skin set from a filesystem path. */
    public static List<Skin> loadFromFile(final Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8));
    }

    /** Parses a JSON skin set document. */
    public static List<Skin> parse(final String json) {
        final String stripped = Json.stripComments(json);
        final int version = Json.readIntField(stripped, "formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported SkinSet formatVersion: " + version + " (expected " + FORMAT_VERSION + ")");
        }
        final String arrayBody = Json.extractArrayBody(stripped, "skins");
        if (arrayBody == null) {
            throw new IllegalArgumentException("Missing array field: skins");
        }
        final List<Skin> out = new ArrayList<>();
        for (final String body : Json.splitObjects(arrayBody)) {
            final String id    = Json.readStringField(body, "id");
            final String name  = Json.readStringField(body, "name");
            final String image = Json.readStringField(body, "image");
            out.add(new Skin(id, name, image));
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // Writing
    // -------------------------------------------------------------------------

    /** Saves the skin set to {@code destination} (UTF-8, overwrites). */
    public static void saveToFile(final List<Skin> skins, final Path destination) throws IOException {
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.writeString(destination, toJson(skins), StandardCharsets.UTF_8);
    }

    /** Serialises the skin set to a pretty-printed JSON string. */
    public static String toJson(final List<Skin> skins) {
        final StringBuilder sb = new StringBuilder(256 + 96 * skins.size());
        sb.append("{\n");
        sb.append("  \"formatVersion\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"skins\": [\n");
        for (int i = 0; i < skins.size(); i++) {
            final Skin s = skins.get(i);
            sb.append("    { \"id\": \"")  .append(Json.escape(s.id()))
              .append("\", \"name\": \"")  .append(Json.escape(s.name()))
              .append("\", \"image\": \"") .append(Json.escape(s.imageRef()))
              .append("\" }");
            if (i < skins.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }
}
