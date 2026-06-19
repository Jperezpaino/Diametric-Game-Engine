package es.noa.rad.map.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.noa.rad.map.Material;

/**
 * Reads / writes <em>material sets</em> &mdash; collections of
 * {@link Material} resources serialised as JSON with an explicit
 * {@code formatVersion}.
 *
 * <p>On-disk shape:</p>
 * <pre>{@code
 * {
 *   "formatVersion": 1,
 *   "materials": [
 *     { "id": "grass", "name": "Grass",
 *       "solid": true, "damage": 0.0, "speedModifier": 1.0,
 *       "properties": { "walkable": true, "causesDrowning": false } },
 *     { "id": "lava", "name": "Lava",
 *       "solid": false, "damage": 5.0, "speedModifier": 0.5,
 *       "properties": { "walkable": false, "causesDrowning": false } }
 *   ]
 * }
 * }</pre>
 *
 * <p>The {@code properties} bag is restricted to boolean / number / string
 * values, mirroring what {@link Material#boolProperty(String, boolean)} et al.
 * consume. Anything richer would warrant a real JSON dependency.</p>
 *
 * @since Phase 8e
 */
public final class MaterialSetIO {

    /** Current on-disk format version emitted by {@link #toJson(List)}. */
    public static final int FORMAT_VERSION = 1;

    private static final Pattern PROP_BOOL = Pattern.compile(
        "\"([^\"]+)\"\\s*:\\s*(true|false)");
    private static final Pattern PROP_NUM = Pattern.compile(
        "\"([^\"]+)\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern PROP_STR = Pattern.compile(
        "\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private MaterialSetIO() {}

    // -------------------------------------------------------------------------
    // Reading
    // -------------------------------------------------------------------------

    /** Loads a material set from a classpath resource (e.g. {@code "sets/materials.json"}). */
    public static List<Material> loadFromClasspath(final String classpathResource) throws IOException {
        try (InputStream in = MaterialSetIO.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IOException("Material set resource not found on classpath: " + classpathResource);
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /** Loads a material set from a filesystem path. */
    public static List<Material> loadFromFile(final Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8));
    }

    /** Parses a JSON material set document. */
    public static List<Material> parse(final String json) {
        final String stripped = Json.stripComments(json);
        final int version = Json.readIntField(stripped, "formatVersion");
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported MaterialSet formatVersion: " + version + " (expected " + FORMAT_VERSION + ")");
        }
        final String arrayBody = Json.extractArrayBody(stripped, "materials");
        if (arrayBody == null) {
            throw new IllegalArgumentException("Missing array field: materials");
        }
        final List<Material> out = new ArrayList<>();
        for (final String body : Json.splitObjects(arrayBody)) {
            final String  id            = Json.readStringField(body, "id");
            final String  name          = Json.readStringField(body, "name");
            final boolean solid         = Json.readBoolField(body,  "solid",         false);
            final float   damage        = Json.readFloatField(body, "damage",        0f);
            final float   speedModifier = Json.readFloatField(body, "speedModifier", 1f);
            final Map<String, Object> properties = parseProperties(body);
            out.add(new Material(id, name, solid, damage, speedModifier, properties));
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // Writing
    // -------------------------------------------------------------------------

    /** Saves the material set to {@code destination} (UTF-8, overwrites). */
    public static void saveToFile(final List<Material> materials, final Path destination) throws IOException {
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.writeString(destination, toJson(materials), StandardCharsets.UTF_8);
    }

    /** Serialises the material set to a pretty-printed JSON string. */
    public static String toJson(final List<Material> materials) {
        final StringBuilder sb = new StringBuilder(512 + 160 * materials.size());
        sb.append("{\n");
        sb.append("  \"formatVersion\": ").append(FORMAT_VERSION).append(",\n");
        sb.append("  \"materials\": [\n");
        for (int i = 0; i < materials.size(); i++) {
            final Material m = materials.get(i);
            sb.append("    {\n");
            sb.append("      \"id\":            \"").append(Json.escape(m.id())).append("\",\n");
            sb.append("      \"name\":          \"").append(Json.escape(m.name())).append("\",\n");
            sb.append("      \"solid\":         ").append(m.solid()).append(",\n");
            sb.append("      \"damage\":        ").append(formatFloat(m.damage())).append(",\n");
            sb.append("      \"speedModifier\": ").append(formatFloat(m.speedModifier())).append(",\n");
            sb.append("      \"properties\":    ");
            appendProperties(sb, m.properties());
            sb.append('\n');
            sb.append("    }");
            if (i < materials.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Properties bag helpers
    // -------------------------------------------------------------------------

    private static Map<String, Object> parseProperties(final String materialBody) {
        final String propsBody = Json.extractObjectBody(materialBody, "properties");
        if (propsBody == null || propsBody.isBlank()) return Map.of();

        final Map<String, Object> out = new LinkedHashMap<>();
        Matcher bm = PROP_BOOL.matcher(propsBody);
        while (bm.find()) out.put(bm.group(1), Boolean.parseBoolean(bm.group(2)));

        Matcher nm = PROP_NUM.matcher(propsBody);
        while (nm.find()) {
            final String key = nm.group(1);
            if (out.containsKey(key)) continue; // bool already wins
            final String raw = nm.group(2);
            out.put(key, raw.contains(".") ? (Object) Float.parseFloat(raw) : (Object) Integer.parseInt(raw));
        }

        Matcher sm = PROP_STR.matcher(propsBody);
        while (sm.find()) {
            final String key = sm.group(1);
            if (out.containsKey(key)) continue;
            out.put(key, sm.group(2));
        }
        return out;
    }

    private static void appendProperties(final StringBuilder sb, final Map<String, Object> properties) {
        if (properties.isEmpty()) {
            sb.append("{}");
            return;
        }
        sb.append("{ ");
        int i = 0;
        for (final Map.Entry<String, Object> e : properties.entrySet()) {
            sb.append('"').append(Json.escape(e.getKey())).append("\": ");
            final Object v = e.getValue();
            if      (v instanceof Boolean b) sb.append(b);
            else if (v instanceof Number  n) sb.append(formatFloat(n.floatValue()));
            else                              sb.append('"').append(Json.escape(String.valueOf(v))).append('"');
            if (i++ < properties.size() - 1) sb.append(", ");
        }
        sb.append(" }");
    }

    private static String formatFloat(final float v) {
        // Avoid scientific notation; trim trailing zeros but keep at least one decimal.
        if (v == (int) v) return Integer.toString((int) v) + ".0";
        return Float.toString(v);
    }
}
