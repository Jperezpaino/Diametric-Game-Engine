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
     * Builds a 32x32 demo map showcasing all 14 {@link TileShape}s, several
     * {@link TileMaterial}s and enough varied terrain for camera-scroll
     * testing (Phase 4). Layout:
     *
     * <pre>
     *   rows  0-31 : grass FLOOR base carpet.
     *   row   4    : all 14 shapes in sequence (cols 1-14) on stone floor.
     *   rows 10-11 : sand path (cols 2-13) crossing the map horizontally.
     *   rows 14-15 : mud patch (cols 4-9).
     *   rows 18-19 : snow patch (cols 12-22).
     *   row  22    : 5 wood BLOCK walk-on-top (cols 2-6).
     *   rows 25-26 : water lake (cols 5-10).
     *   col  28    : lava river (rows 5-25).
     *   rows 8-9, cols 16-19: ramp hill (RAMP shapes + elevated stone).
     * </pre>
     */
    public static TileMap createDemoMap() {
        final int w = 32;
        final int d = 32;
        final TileLayer L = new TileLayer(w, d);

        // ---- base carpet of GRASS FLOOR at z=0 --------------------------------
        for (int row = 0; row < d; row++) {
            for (int col = 0; col < w; col++) {
                L.setTile(col, row, 0, TileMaterial.GRASS, TileShape.FLOOR);
            }
        }

        // ---- showcase row: all 14 shapes (cols 1-14, row 4) -------------------
        final TileShape[] showcase = {
                TileShape.FLOOR, TileShape.BLOCK,
                TileShape.DOUBLE_RAMP_N, TileShape.DOUBLE_RAMP_E,
                TileShape.DOUBLE_RAMP_S, TileShape.DOUBLE_RAMP_W,
                TileShape.RAMP_SW, TileShape.RAMP_NW,
                TileShape.RAMP_NE, TileShape.RAMP_SE,
                TileShape.CONCAVE_N, TileShape.CONCAVE_E,
                TileShape.CONCAVE_S, TileShape.CONCAVE_W
        };
        for (int i = 0; i < showcase.length; i++) {
            final int col = i + 1;
            L.setTile(col, 4, 0, TileMaterial.STONE, TileShape.FLOOR);
            if (showcase[i] != TileShape.FLOOR) {
                L.setTile(col, 4, 1, TileMaterial.STONE, showcase[i]);
            }
        }

        // ---- sand path (rows 10-11, cols 2-13) --------------------------------
        for (int row = 10; row <= 11; row++) {
            for (int col = 2; col <= 13; col++) {
                L.setTile(col, row, 0, TileMaterial.SAND, TileShape.FLOOR);
            }
        }

        // ---- mud patch (rows 14-15, cols 4-9) ---------------------------------
        for (int row = 14; row <= 15; row++) {
            for (int col = 4; col <= 9; col++) {
                L.setTile(col, row, 0, TileMaterial.MUD, TileShape.FLOOR);
            }
        }

        // ---- snow patch (rows 18-19, cols 12-22) ------------------------------
        for (int row = 18; row <= 19; row++) {
            for (int col = 12; col <= 22; col++) {
                L.setTile(col, row, 0, TileMaterial.SNOW, TileShape.FLOOR);
            }
        }

        // ---- walk-on-top blocks: 5 wood BLOCKs (row 22, cols 2-6) -------------
        for (int col = 2; col <= 6; col++) {
            L.setTile(col, 22, 1, TileMaterial.WOOD, TileShape.BLOCK);
        }

        // ---- water lake (rows 25-26, cols 5-10) --------------------------------
        for (int row = 25; row <= 26; row++) {
            for (int col = 5; col <= 10; col++) {
                L.setTile(col, row, 0, TileMaterial.WATER, TileShape.FLOOR);
            }
        }

        // ---- lava river (col 28, rows 5-25) ------------------------------------
        for (int row = 5; row <= 25; row++) {
            L.setTile(28, row, 0, TileMaterial.LAVA, TileShape.FLOOR);
        }

        // ---- ramp hill (rows 8-9, cols 16-19) ----------------------------------
        // Elevated stone platform at z=1, approached by ramps.
        for (int col = 17; col <= 18; col++) {
            for (int row = 8; row <= 9; row++) {
                L.setTile(col, row, 0, TileMaterial.STONE, TileShape.FLOOR);
                L.setTile(col, row, 1, TileMaterial.STONE, TileShape.BLOCK);
            }
        }
        // West ramp approach (col 16).
        L.setTile(16, 8,  0, TileMaterial.STONE, TileShape.FLOOR);
        L.setTile(16, 8,  1, TileMaterial.STONE, TileShape.DOUBLE_RAMP_E);
        L.setTile(16, 9,  0, TileMaterial.STONE, TileShape.FLOOR);
        L.setTile(16, 9,  1, TileMaterial.STONE, TileShape.DOUBLE_RAMP_E);
        // East ramp descent (col 19).
        L.setTile(19, 8,  0, TileMaterial.STONE, TileShape.FLOOR);
        L.setTile(19, 8,  1, TileMaterial.STONE, TileShape.DOUBLE_RAMP_W);
        L.setTile(19, 9,  0, TileMaterial.STONE, TileShape.FLOOR);
        L.setTile(19, 9,  1, TileMaterial.STONE, TileShape.DOUBLE_RAMP_W);

        return new TileMap(L);
    }
}