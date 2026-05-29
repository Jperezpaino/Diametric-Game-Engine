package es.noa.rad.projection;

import es.noa.rad.config.GameConfig;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileShape;

/**
 * Diametric (2:1) projection utility.
 *
 * <p>Centralises every conversion between world coordinates (col, row, z) and
 * screen coordinates (x, y). No other class in the engine should perform this
 * math directly.</p>
 *
 * <p><b>Projection formula (world → screen):</b></p>
 * <pre>
 *   screenX = (col - row) * (TILE_WIDTH  / 2)
 *   screenY = (col + row) * (TILE_HEIGHT / 2) - z * Z_STEP_PX
 * </pre>
 *
 * <p><b>Inverse formula (screen → world, assuming z = 0):</b></p>
 * <pre>
 *   col = (screenX / (TILE_WIDTH / 2) + screenY / (TILE_HEIGHT / 2)) / 2
 *   row = (screenY / (TILE_HEIGHT / 2) - screenX / (TILE_WIDTH  / 2)) / 2
 * </pre>
 *
 * @since Phase 1
 */
public final class IsoProjection {

    private final int halfTileWidth;
    private final int halfTileHeight;
    private final int zStep;

    /** Creates a projection bound to the constants in {@link GameConfig}. */
    public IsoProjection() {
        this.halfTileWidth  = GameConfig.TILE_WIDTH  / 2;
        this.halfTileHeight = GameConfig.TILE_HEIGHT / 2;
        this.zStep          = GameConfig.Z_STEP_PX;
    }

    /**
     * Converts a world coordinate to a screen coordinate.
     *
     * @param col column in tile units
     * @param row row in tile units
     * @param z   height in tile units
     * @return screen point in pixels
     */
    public ScreenPoint worldToScreen(final float col, final float row, final float z) {
        final int sx = Math.round((col - row) * halfTileWidth);
        final int sy = Math.round((col + row) * halfTileHeight - z * zStep);
        return new ScreenPoint(sx, sy);
    }

    /**
     * Converts a world point to a screen point.
     *
     * @param point world point
     * @return screen point in pixels
     */
    public ScreenPoint worldToScreen(final WorldPoint point) {
        return worldToScreen(point.getCol(), point.getRow(), point.getZ());
    }

    /**
     * Converts a tile position to a screen point, taking
     * {@link Tile#elevation()} as the {@code z} component.
     *
     * @param tile tile to project (may not be {@code null})
     * @param col  column of the tile
     * @param row  row of the tile
     * @return screen point in pixels
     */
    public ScreenPoint worldToScreen(final Tile tile, final int col, final int row) {
        return worldToScreen(col, row, tile.elevation());
    }

    /**
     * Projects one of the four diamond corners of a tile&#39;s top surface.
     * Combines the (col,row,elevation) of the tile with the corner offset
     * (±0.5 in col / row) and the corner&#39;s sub-tile height contributed
     * by the shape.
     *
     * @param tile   tile providing material/shape/elevation
     * @param col    tile column
     * @param row    tile row
     * @param corner which top-face corner to project
     * @return screen pixel for that corner
     */
    public ScreenPoint projectCorner(final Tile tile, final int col, final int row,
                                     final TileShape.Corner corner) {
        final float dCol;
        final float dRow;
        switch (corner) {
            case NE: dCol =  0.5f; dRow = -0.5f; break;
            case SE: dCol =  0.5f; dRow =  0.5f; break;
            case SW: dCol = -0.5f; dRow =  0.5f; break;
            case NW: dCol = -0.5f; dRow = -0.5f; break;
            default: throw new IllegalStateException();
        }
        final float z = tile.elevation() + tile.shape().cornerHeight(corner);
        return worldToScreen(col + dCol, row + dRow, z);
    }

    /**
     * Inverse projection: converts a screen pixel back to world coordinates,
     * assuming the target lies on the ground plane (z = 0).
     *
     * @param sx horizontal pixel
     * @param sy vertical pixel
     * @return world point with z = 0
     */
    public WorldPoint screenToWorld(final int sx, final int sy) {
        final float a = (float) sx / halfTileWidth;
        final float b = (float) sy / halfTileHeight;
        final float col = (a + b) / 2f;
        final float row = (b - a) / 2f;
        return new WorldPoint(col, row, 0f);
    }
}
