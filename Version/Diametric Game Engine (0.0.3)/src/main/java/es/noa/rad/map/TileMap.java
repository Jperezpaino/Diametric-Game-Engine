package es.noa.rad.map;

/**
 * Aggregate of every layer that conforms a playable map.
 *
 * <p>From Phase 3 onwards the map exposes two layers stacked back-to-front:</p>
 * <ol>
 *   <li><b>visual layer</b> — ground tiles (grass, water, walls, elevated…).</li>
 *   <li><b>object layer</b> — decorations and props rendered on top of the
 *       ground; may contain {@code null} cells where nothing exists.</li>
 * </ol>
 *
 * @since Phase 1 (object layer added Phase 3)
 */
public final class TileMap {

    private final int width;
    private final int depth;
    private final TileLayer visualLayer;
    private final TileLayer objectLayer;

    /**
     * Creates a tile map with the given dimensions and two empty layers.
     *
     * @param width number of columns
     * @param depth number of rows
     */
    public TileMap(final int width, final int depth) {
        this.width = width;
        this.depth = depth;
        this.visualLayer = new TileLayer(width, depth);
        this.objectLayer = new TileLayer(width, depth);
    }

    public int       getWidth()       { return width; }
    public int       getDepth()       { return depth; }
    public TileLayer getVisualLayer() { return visualLayer; }
    public TileLayer getObjectLayer() { return objectLayer; }

    /**
     * Builds the hardcoded 13×11 demo map used to showcase Phase 3.
     *
     * <p>Layout (looking at the screen, N up):</p>
     * <ul>
     *   <li>Outer border of {@link TileType#WALL}.</li>
     *   <li>Default inner area of {@link TileType#GRASS} at z = 0.</li>
     *   <li>Four elevated platforms, each reachable by one of the four
     *       <i>edge ramps</i>:
     *       <ul>
     *         <li>{@link TileType#RAMP_NW} at (2, 2) → {@link TileType#ELEVATED} at (1, 2).</li>
     *         <li>{@link TileType#RAMP_NE} at (5, 2) → {@link TileType#ELEVATED} at (5, 1).</li>
     *         <li>{@link TileType#RAMP_SE} at (9, 5) → {@link TileType#ELEVATED} at (10, 5).</li>
     *         <li>{@link TileType#RAMP_SW} at (5, 8) → {@link TileType#ELEVATED} at (5, 9).</li>
     *       </ul>
     *   </li>
     *   <li>The four <i>corner ramps</i> ({@link TileType#RAMP_N},
     *       {@link TileType#RAMP_S}, {@link TileType#RAMP_E},
     *       {@link TileType#RAMP_W}) are placed in the eastern showcase row
     *       as visual references; only the low corners can be approached.</li>
     *   <li>Two {@link TileType#WATER} hazards at (3, 5) and (7, 5).</li>
     * </ul>
     *
     * @return demo tile map
     */
    public static TileMap createDemoMap() {
        final int width = 13;
        final int depth = 11;
        final TileMap map = new TileMap(width, depth);
        final TileLayer layer = map.getVisualLayer();

        // 1. Border of walls + interior of grass.
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < depth; row++) {
                final boolean border = col == 0 || row == 0
                        || col == width - 1 || row == depth - 1;
                final TileType type = border ? TileType.WALL : TileType.GRASS;
                layer.setTile(col, row, new Tile(type));
            }
        }

        // 2. Edge ramps + elevated platforms.

        // 2a. RAMP_NW at (2, 2): high WN edge connects to ELEVATED at (1, 2).
        layer.setTile(1, 2, new Tile(TileType.ELEVATED, 1));
        layer.setTile(2, 2, new Tile(TileType.RAMP_NW, 0));

        // 2b. RAMP_NE at (5, 2): high NE edge connects to ELEVATED at (5, 1).
        layer.setTile(5, 1, new Tile(TileType.ELEVATED, 1));
        layer.setTile(5, 2, new Tile(TileType.RAMP_NE, 0));

        // 2c. RAMP_SE at (9, 5): high ES edge connects to ELEVATED at (10, 5).
        layer.setTile(10, 5, new Tile(TileType.ELEVATED, 1));
        layer.setTile(9,  5, new Tile(TileType.RAMP_SE, 0));

        // 2d. RAMP_SW at (5, 8): high SW edge connects to ELEVATED at (5, 9).
        layer.setTile(5, 9, new Tile(TileType.ELEVATED, 1));
        layer.setTile(5, 8, new Tile(TileType.RAMP_SW, 0));

        // 3. Corner ramps (visual showcase, no plateau on the high side).
        layer.setTile(8,  2, new Tile(TileType.RAMP_N, 0));
        layer.setTile(10, 2, new Tile(TileType.RAMP_S, 0));
        layer.setTile(8,  8, new Tile(TileType.RAMP_E, 0));
        layer.setTile(10, 8, new Tile(TileType.RAMP_W, 0));

        // 4. Water hazards in the central corridor.
        layer.setTile(3, 5, new Tile(TileType.WATER));
        layer.setTile(7, 5, new Tile(TileType.WATER));

        return map;
    }
}
