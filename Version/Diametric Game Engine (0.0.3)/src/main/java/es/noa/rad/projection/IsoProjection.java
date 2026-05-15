package es.noa.rad.projection;

import es.noa.rad.config.GameConfig;
import es.noa.rad.map.Tile;

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
 *   screenY = (col + row) * (TILE_HEIGHT / 2) - z * TILE_HEIGHT
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
    private final int tileHeight;

    /** Creates a projection bound to the constants in {@link GameConfig}. */
    public IsoProjection() {
        this.halfTileWidth = GameConfig.TILE_WIDTH / 2;
        this.halfTileHeight = GameConfig.TILE_HEIGHT / 2;
        this.tileHeight = GameConfig.TILE_HEIGHT;
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
        final int sy = Math.round((col + row) * halfTileHeight - z * tileHeight);
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
     * {@link Tile#getElevation()} as the {@code z} component.
     *
     * @param tile tile to project (may not be {@code null})
     * @param col  column of the tile
     * @param row  row of the tile
     * @return screen point in pixels
     */
    public ScreenPoint worldToScreen(final Tile tile, final int col, final int row) {
        return worldToScreen(col, row, tile.getElevation());
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
