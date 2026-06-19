package es.noa.rad.map;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 3D voxel layer of tiles. Cells are addressed by (col, row, z).
 * Storage is sparse: per (col,row) a TreeMap holds (z to tile),
 * so empty Z slots use no memory.
 *
 * @since Phase 1 (made voxel in Phase 3)
 */
public final class TileLayer {

    private final int width;
    private final int depth;
    private final TreeMap<Integer, Tile>[][] columns;

    @SuppressWarnings("unchecked")
    public TileLayer(final int width, final int depth) {
        this.width = width;
        this.depth = depth;
        this.columns = new TreeMap[width][depth];
    }

    public int getWidth() { return width; }
    public int getDepth() { return depth; }

    public boolean inBounds(final int col, final int row) {
        return col >= 0 && row >= 0 && col < width && row < depth;
    }

    public Tile getTile(final int col, final int row, final int z) {
        if (!inBounds(col, row)) return null;
        final TreeMap<Integer, Tile> stack = columns[col][row];
        return stack == null ? null : stack.get(z);
    }

    public Tile getTopTile(final int col, final int row) {
        if (!inBounds(col, row)) return null;
        final TreeMap<Integer, Tile> stack = columns[col][row];
        if (stack == null || stack.isEmpty()) return null;
        return stack.lastEntry().getValue();
    }

    public Tile getFloorTileAt(final int col, final int row, final int z) {
        if (!inBounds(col, row)) return null;
        final TreeMap<Integer, Tile> stack = columns[col][row];
        if (stack == null) return null;
        final Map.Entry<Integer, Tile> floor = stack.floorEntry(z);
        return floor == null ? null : floor.getValue();
    }

    public List<Tile> getColumn(final int col, final int row) {
        if (!inBounds(col, row)) return Collections.emptyList();
        final TreeMap<Integer, Tile> stack = columns[col][row];
        if (stack == null) return Collections.emptyList();
        return List.copyOf(stack.values());
    }

    public void setTile(final int col, final int row, final Tile tile) {
        if (!inBounds(col, row) || tile == null) return;
        TreeMap<Integer, Tile> stack = columns[col][row];
        if (stack == null) {
            stack = new TreeMap<>();
            columns[col][row] = stack;
        }
        stack.put(tile.elevation(), tile);
    }

    public void setTile(final int col, final int row, final int z,
                        final TileMaterial material, final TileShape shape) {
        setTile(col, row, new Tile(material, shape, z));
    }

    public void clearTile(final int col, final int row, final int z) {
        if (!inBounds(col, row)) return;
        final TreeMap<Integer, Tile> stack = columns[col][row];
        if (stack != null) {
            stack.remove(z);
            if (stack.isEmpty()) columns[col][row] = null;
        }
    }

    public void forEachTile(final TileVisitor visitor) {
        for (int row = 0; row < depth; row++) {
            for (int col = 0; col < width; col++) {
                final TreeMap<Integer, Tile> stack = columns[col][row];
                if (stack == null) continue;
                for (final Map.Entry<Integer, Tile> e : stack.entrySet()) {
                    visitor.visit(col, row, e.getKey(), e.getValue());
                }
            }
        }
    }

    @FunctionalInterface
    public interface TileVisitor {
        void visit(int col, int row, int z, Tile tile);
    }
}