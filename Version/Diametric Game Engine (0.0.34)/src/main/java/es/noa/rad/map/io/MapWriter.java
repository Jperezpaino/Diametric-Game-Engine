package es.noa.rad.map.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileMaterial;
import es.noa.rad.map.TileShape;

/**
 * Serialises a {@link TileMap} back to the same JSON format read by
 * {@link MapLoader}.
 *
 * <p>The writer scans every voxel to derive a minimal palette of
 * (material, shape) pairs in order of first appearance, then emits one
 * {@code {"data":[...]}} block per Z level. Empty cells use {@code -1}.
 * The output round-trips through {@link MapLoader#parse(String)}.</p>
 *
 * @since Phase 7a
 */
public final class MapWriter {

    /** Palette key: (material, shape) pair. */
    private record PaletteKey(TileMaterial material, TileShape shape) {}

    private MapWriter() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Serialises {@code map} to a pretty-printed JSON string. */
    public static String toJson(final TileMap map) {
        final TileLayer layer = map.getLayer();
        final int width = layer.getWidth();
        final int depth = layer.getDepth();

        final List<PaletteKey>            palette = new ArrayList<>();
        final Map<PaletteKey, Integer>    paletteIndex = new HashMap<>();
        final int length = scanPaletteAndDepth(layer, palette, paletteIndex);

        final StringBuilder sb = new StringBuilder(8192);
        sb.append("{\n");
        sb.append("  \"width\":  ").append(width) .append(",\n");
        sb.append("  \"depth\":  ").append(depth) .append(",\n");
        sb.append("  \"length\": ").append(length).append(",\n");
        appendPalette(sb, palette);
        sb.append(",\n");
        appendTileLayer(sb, layer, paletteIndex, width, depth, length);
        sb.append("\n}\n");
        return sb.toString();
    }

    /** Writes {@code map} as JSON to {@code destination} (UTF-8, overwrites). */
    public static void saveToFile(final TileMap map, final Path destination) throws IOException {
        if (destination.getParent() != null) {
            Files.createDirectories(destination.getParent());
        }
        Files.writeString(destination, toJson(map), StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    /** Walks the layer to build the palette (in first-seen order) and returns max Z + 1. */
    private static int scanPaletteAndDepth(final TileLayer layer,
                                            final List<PaletteKey> palette,
                                            final Map<PaletteKey, Integer> index) {
        final int[] maxZ = { 0 };
        layer.forEachTile((col, row, z, tile) -> {
            final PaletteKey key = new PaletteKey(tile.material(), tile.shape());
            if (!index.containsKey(key)) {
                index.put(key, palette.size());
                palette.add(key);
            }
            if (z > maxZ[0]) maxZ[0] = z;
        });
        return maxZ[0] + 1;
    }

    private static void appendPalette(final StringBuilder sb, final List<PaletteKey> palette) {
        sb.append("  \"tileMap\": [\n");
        for (int i = 0; i < palette.size(); i++) {
            final PaletteKey k = palette.get(i);
            sb.append("    { \"tile\": { \"index\": ").append(i)
              .append(", \"material\": \"TileMaterial.").append(k.material().name())
              .append("\", \"Shape\": \"TileShape.")    .append(k.shape().name())
              .append("\" } }");
            if (i < palette.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]");
    }

    private static void appendTileLayer(final StringBuilder sb, final TileLayer layer,
                                         final Map<PaletteKey, Integer> index,
                                         final int width, final int depth, final int length) {
        sb.append("  \"tileLayer\": [\n");
        for (int z = 0; z < length; z++) {
            sb.append("    {\n");
            sb.append("      \"data\": [\n");
            for (int row = 0; row < depth; row++) {
                sb.append("        ");
                for (int col = 0; col < width; col++) {
                    final Tile t = layer.getTile(col, row, z);
                    final int idx = (t == null)
                        ? -1
                        : index.getOrDefault(new PaletteKey(t.material(), t.shape()), -1);
                    sb.append(String.format("%2d", idx));
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
}
