package es.noa.rad.map;

/**
 * 2D grid of {@link Tile} instances representing a single layer of a map.
 *
 * <p>Indexing convention: {@code tiles[col][row]} where {@code col} is the
 * X axis and {@code row} is the depth axis. This matches the world-coordinate
 * system used throughout the engine.</p>
 *
 * @since Phase 1
 */
public final class TileLayer {

    private final int width;
    private final int depth;
    private final Tile[][] tiles;

    /**
     * Creates an empty layer of the given dimensions.
     *
     * @param width number of columns
     * @param depth number of rows
     */
    public TileLayer(final int width, final int depth) {
        this.width = width;
        this.depth = depth;
        this.tiles = new Tile[width][depth];
    }

    public int getWidth() { return width; }
    public int getDepth() { return depth; }

    /**
     * Returns the tile at the given grid position.
     *
     * @param col column index
     * @param row row index
     * @return tile or {@code null} if not assigned
     */
    public Tile getTile(final int col, final int row) {
        if (col < 0 || row < 0 || col >= width || row >= depth) {
            return null;
        }
        return tiles[col][row];
    }

    /**
     * Stores a tile at the given grid position.
     *
     * @param col  column index
     * @param row  row index
     * @param tile tile to store
     */
    public void setTile(final int col, final int row, final Tile tile) {
        tiles[col][row] = tile;
    }
}
