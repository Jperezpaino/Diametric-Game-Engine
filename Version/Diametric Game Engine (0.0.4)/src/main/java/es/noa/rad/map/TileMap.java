package es.noa.rad.map;

/**
 * The world map: a single voxel {@link TileLayer} containing every tile of
 * the playable area. The split between &quot;visual&quot; and &quot;object&quot;
 * layers used in earlier phases has been replaced by Z stacking inside the
 * voxel layer.
 *
 * @since Phase 1 (voxel rewrite Phase 3)
 */
public final class TileMap {

    private final TileLayer layer;

    public TileMap(final TileLayer layer) {
        this.layer = layer;
    }

    public TileLayer getLayer() { return layer; }

    /** Convenience: top-most tile at (col,row), or {@code null}. */
    public Tile getTopTile(final int col, final int row) {
        return layer.getTopTile(col, row);
    }

    /** Convenience: tile at exactly (col,row,z), or {@code null}. */
    public Tile getTile(final int col, final int row, final int z) {
        return layer.getTile(col, row, z);
    }

    public int getWidth() { return layer.getWidth(); }
    public int getDepth() { return layer.getDepth(); }

    // -------------------------------------------------------------------------
    // Demo map factory
    // -------------------------------------------------------------------------

    /**
     * Builds a 16x16 demo map showcasing all 14 {@link TileShape}s and a few
     * materials. Layout (each cell is one tile):
     *
     * <pre>
     *   row 0..1 : grass FLOOR carpet (border).
     *   row 2..2 : the 14 shapes laid out in a single line on a stone floor,
     *              so the player can walk along it and feel each slope.
     *   row 4..4 : a row of BLOCKs (wood) demonstrating walk-on-top.
     *   rest     : grass FLOOR.
     * </pre>
     *
     * <p>For the slope row each tile is built on a STONE FLOOR at z=0 and the
     * shape sits at z=1 so that the slopes start &quot;on top of&quot; the
     * floor (this matches the look of the reference renders).</p>
     */
    public static TileMap createDemoMap() {
        final int w = 16;
        final int d = 16;
        final TileLayer L = new TileLayer(w, d);

        // Carpet of grass at z=0 everywhere.
        for (int row = 0; row < d; row++) {
            for (int col = 0; col < w; col++) {
                L.setTile(col, row, 0, TileMaterial.GRASS, TileShape.FLOOR);
            }
        }

        // Showcase row: all 14 shapes in order, on top of stone floor.
        final TileShape[] showcase = {
                TileShape.FLOOR, TileShape.BLOCK,
                TileShape.DOUBLE_RAMP_N, TileShape.DOUBLE_RAMP_E,
                TileShape.DOUBLE_RAMP_S, TileShape.DOUBLE_RAMP_W,
                TileShape.RAMP_SW, TileShape.RAMP_NW,
                TileShape.RAMP_NE, TileShape.RAMP_SE,
                TileShape.CONCAVE_N, TileShape.CONCAVE_E,
                TileShape.CONCAVE_S, TileShape.CONCAVE_W
        };
        for (int i = 0; i < showcase.length && (i + 1) < w; i++) {
            final int col = i + 1;
            final int row = 6;
            // stone base at z=0 (already grass; replace).
            L.setTile(col, row, 0, TileMaterial.STONE, TileShape.FLOOR);
            // shape on top at z=1 (skip if it is FLOOR to keep the gap visible).
            if (showcase[i] != TileShape.FLOOR) {
                L.setTile(col, row, 1, TileMaterial.STONE, showcase[i]);
            }
        }

        // Walk-on-top row: 5 wood blocks.
        for (int i = 0; i < 5; i++) {
            final int col = 2 + i;
            final int row = 10;
            L.setTile(col, row, 1, TileMaterial.WOOD, TileShape.BLOCK);
        }

        // A small water puddle (non-walkable).
        L.setTile(8, 12, 0, TileMaterial.WATER, TileShape.FLOOR);
        L.setTile(9, 12, 0, TileMaterial.WATER, TileShape.FLOOR);

        // A small lava patch (damaging).
        L.setTile(12, 12, 0, TileMaterial.LAVA, TileShape.FLOOR);

        return new TileMap(L);
    }
}