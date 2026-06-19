package es.noa.rad.map.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiny hand-rolled JSON helpers shared by every reader/writer in
 * {@code es.noa.rad.map.io}.
 *
 * <p>The project deliberately avoids any JSON dependency: every resource
 * format we serialise (maps, skin sets, material sets, tile sets) is small
 * and shape-predictable, so a handful of regex primitives keeps the runtime
 * dependency-free and trivially auditable.</p>
 *
 * <p>The kernel only covers what the engine actually emits or consumes:</p>
 * <ul>
 *   <li>Stripping {@code //} and {@code /* *}{@code /} comments for leniency.</li>
 *   <li>Reading scalar fields by key (int / string / bool / float, optional or required).</li>
 *   <li>Extracting the body of a named array / object via balanced-brace walking.</li>
 *   <li>Splitting an array body into per-object substrings.</li>
 *   <li>Escaping strings for safe emission.</li>
 * </ul>
 *
 * <p>Anything more elaborate (nested deep schemas, streaming, etc.) is out
 * of scope: a real dependency would beat us on every axis at that point.</p>
 *
 * @since Phase 8e
 */
final class Json {

    private Json() {}

    /** Removes {@code //} line and {@code /* *}{@code /} block comments so callers can be lenient. */
    static String stripComments(final String src) {
        String s = src.replaceAll("(?m)//.*$", "");
        s = s.replaceAll("(?s)/\\*.*?\\*/", "");
        return s;
    }

    // -------------------------------------------------------------------------
    // Scalar field readers
    // -------------------------------------------------------------------------

    /** Reads a required integer field; throws {@link IllegalArgumentException} when missing. */
    static int readIntField(final String body, final String key) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)")
            .matcher(body);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing numeric field: " + key);
        }
        return Integer.parseInt(m.group(1));
    }

    /** Reads an integer field, falling back to {@code defaultValue} when absent. */
    static int readIntField(final String body, final String key, final int defaultValue) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)")
            .matcher(body);
        return m.find() ? Integer.parseInt(m.group(1)) : defaultValue;
    }

    /** Reads a required string field; throws when missing. */
    static String readStringField(final String body, final String key) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            .matcher(body);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing string field: " + key);
        }
        return unescape(m.group(1));
    }

    /** Reads an optional string field, falling back to {@code defaultValue} when absent. */
    static String readOptionalStringField(final String body, final String key, final String defaultValue) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            .matcher(body);
        return m.find() ? unescape(m.group(1)) : defaultValue;
    }

    /** Reads a boolean field, falling back to {@code defaultValue} when absent. */
    static boolean readBoolField(final String body, final String key, final boolean defaultValue) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)")
            .matcher(body);
        return m.find() ? Boolean.parseBoolean(m.group(1)) : defaultValue;
    }

    /** Reads a numeric field as float, falling back to {@code defaultValue} when absent. */
    static float readFloatField(final String body, final String key, final float defaultValue) {
        final Matcher m = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
            .matcher(body);
        return m.find() ? Float.parseFloat(m.group(1)) : defaultValue;
    }

    // -------------------------------------------------------------------------
    // Body extraction (brace / bracket aware)
    // -------------------------------------------------------------------------

    /** Returns the body inside the array referenced by {@code key}, or {@code null} when absent. */
    static String extractArrayBody(final String json, final String key) {
        return extractBracketBody(json, key, '[', ']');
    }

    /** Returns the body inside the object referenced by {@code key}, or {@code null} when absent. */
    static String extractObjectBody(final String json, final String key) {
        return extractBracketBody(json, key, '{', '}');
    }

    /**
     * Splits an array body into per-object substrings (the content between
     * each top-level pair of {@code {}}, brace-balance aware).
     */
    static List<String> splitObjects(final String arrayBody) {
        final List<String> out = new ArrayList<>();
        if (arrayBody == null) return out;
        final int n = arrayBody.length();
        int i = 0;
        while (i < n) {
            // Find next '{'
            while (i < n && arrayBody.charAt(i) != '{') i++;
            if (i >= n) break;
            int depth = 1;
            int start = ++i;
            while (i < n && depth > 0) {
                final char c = arrayBody.charAt(i);
                if      (c == '{') depth++;
                else if (c == '}') depth--;
                if (depth == 0) break;
                i++;
            }
            if (depth == 0) {
                out.add(arrayBody.substring(start, i));
                i++;
            }
        }
        return out;
    }

    // -------------------------------------------------------------------------
    // Writer helpers
    // -------------------------------------------------------------------------

    /** Escapes backslashes and double quotes for JSON output. */
    static String escape(final String s) {
        if (s == null) return "";
        final StringBuilder sb = new StringBuilder(s.length() + 2);
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> sb.append(c);
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private static String extractBracketBody(final String json,
                                              final String key,
                                              final char open,
                                              final char close) {
        final Matcher start = Pattern
            .compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\" + open)
            .matcher(json);
        if (!start.find()) return null;
        int depth = 1;
        int i = start.end();
        final StringBuilder body = new StringBuilder();
        while (i < json.length() && depth > 0) {
            final char c = json.charAt(i);
            if      (c == open)  depth++;
            else if (c == close) { depth--; if (depth == 0) break; }
            body.append(c);
            i++;
        }
        if (depth != 0) {
            throw new IllegalArgumentException("Unbalanced '" + open + "' for field: " + key);
        }
        return body.toString();
    }

    private static String unescape(final String s) {
        if (s.indexOf('\\') < 0) return s;
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                final char n = s.charAt(++i);
                switch (n) {
                    case '\\' -> sb.append('\\');
                    case '"'  -> sb.append('"');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    default   -> sb.append(n);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
