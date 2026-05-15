package es.noa.rad.map;

/**
 * Aggregate of every layer that conforms a playable map.
 *
 * <p>In Phase 1 only the visual layer exists. Future phases will add separate
 * collision, height and event layers without breaking this API.</p>
 *
 * @since Phase 1
 */
public final class TileMap {

    private final int width;
    private final int depth;
    private final TileLayer visualLayer;

    /**
     * Creates a tile map with the given dimensions and an empty visual layer.
     *
     * @param width number of columns
     * @param depth number of rows
     */
    public TileMap(final int width, final int depth) {
        this.width = width;
        this.depth = depth;
        this.visualLayer = new TileLayer(width, depth);
    }

    public int getWidth() { return width; }
    public int getDepth() { return depth; }
    public TileLayer getVisualLayer() { return visualLayer; }

    /**
     * Builds the hardcoded 7×7 demo map used in Phase 1:
     * <ul>
     *   <li>Outer border of {@link TileType#WALL}.</li>
     *   <li>Inner area of {@link TileType#GRASS}.</li>
     *   <li>One {@link TileType#WATER} tile at (3, 3).</li>
     * </ul>
     *
     * @return demo tile map
     */
    public static TileMap createDemoMap() {
        final int size = 7;
        final TileMap map = new TileMap(size, size);
        final TileLayer layer = map.getVisualLayer();

        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                final boolean border = col == 0 || row == 0
                        || col == size - 1 || row == size - 1;
                final TileType type = border ? TileType.WALL : TileType.GRASS;
                layer.setTile(col, row, new Tile(type));
            }
        }
        layer.setTile(3, 3, new Tile(TileType.WATER));

        return map;
    }
}
