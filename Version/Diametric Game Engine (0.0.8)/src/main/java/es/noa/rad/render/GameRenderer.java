package es.noa.rad.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import es.noa.rad.camera.Camera;
import es.noa.rad.config.GameConfig;
import es.noa.rad.entity.Player;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMap;
import es.noa.rad.projection.IsoProjection;

/**
 * High-level renderer that orchestrates every drawing step of a frame.
 *
 * <p>Responsibilities:</p>
 * <ol>
 *   <li>Clear the back buffer.</li>
 *   <li>Apply the camera translation.</li>
 *   <li>Render tiles and entities in the correct order.</li>
 *   <li>Draw debug information (FPS / UPS) in screen space.</li>
 * </ol>
 *
 * @since Phase 1
 */
public final class GameRenderer {

    private static final Color BACKGROUND = new Color(20, 20, 30);
    private static final Color HUD_COLOR  = Color.WHITE;
    private static final Font  HUD_FONT   = new Font("Monospaced", Font.PLAIN, 14);

    private final TileRenderer   tileRenderer;
    private final EntityRenderer entityRenderer;
    private final IsoProjection  projection;

    public GameRenderer(final IsoProjection projection) {
        this.tileRenderer   = new TileRenderer(projection);
        this.entityRenderer = new EntityRenderer(projection);
        this.projection     = projection;
    }

    /**
     * Renders a full frame.
     *
     * @param g      graphics context of the active back buffer
     * @param map    tile map
     * @param player player entity
     * @param camera camera
     * @param fps    measured frames per second
     * @param ups    measured updates per second
     */
    public void render(final Graphics2D g, final TileMap map, final Player player,
                       final Camera camera, final int fps, final int ups) {
        // Clear background.
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Collect all drawables (tiles + player) and sort by painter's key ---
        // Painter's order for 2:1 isometric: ascending col+row, then ascending z.
        final float pCol = player.getPosition().getCol();
        final float pRow = player.getPosition().getRow();
        final float pZ   = player.getPosition().getZ();

        // Determine the player's painter key by sampling 5 screen points of the
        // marker diamond (centre + 4 corners), unprojecting each at the player's z
        // to find which tile cell it occupies, then taking the southernmost cell
        // (highest col+row) as the target.  That ensures a tile visually "in front"
        // of any part of the marker will always be drawn on top of the player.
        final es.noa.rad.projection.ScreenPoint pScreen =
                projection.worldToScreen(pCol, pRow, pZ);
        final float halfTileW = GameConfig.TILE_WIDTH  / 2f;
        final float halfTileH = GameConfig.TILE_HEIGHT / 2f;

        // Centre (0,0) + N/E/S/W corners of the diamond marker.
        final int hw = EntityRenderer.MARKER_HALF_WIDTH;
        final int hh = EntityRenderer.MARKER_HALF_HEIGHT;
        final int[][] offsets = { {0, 0}, {0, -hh}, {hw, 0}, {0, hh}, {-hw, 0} };

        // Debug: cell for each sample point [centre, N, E, S, W]
        final int[] dbgCellC = new int[2];
        final int[] dbgCellN = new int[2];
        final int[] dbgCellE = new int[2];
        final int[] dbgCellS = new int[2];
        final int[] dbgCellW = new int[2];
        final int[][]  dbgCells = { dbgCellC, dbgCellN, dbgCellE, dbgCellS, dbgCellW };

        int maxCellSum = Integer.MIN_VALUE;
        for (int i = 0; i < offsets.length; i++) {
            final int sx = pScreen.getX() + offsets[i][0];
            final int sy = pScreen.getY() + offsets[i][1];
            final float cr  = (sy + pZ * GameConfig.Z_STEP_PX) / halfTileH;
            final float cmr = sx / halfTileW;
            final int ic = Math.round((cr + cmr) / 2.0f);
            final int ir = Math.round((cr - cmr) / 2.0f);
            dbgCells[i][0] = ic;
            dbgCells[i][1] = ir;
            maxCellSum = Math.max(maxCellSum, ic + ir);
        }
        final double playerKey = maxCellSum + pZ;

        // Gather tiles.
        record TileEntry(int col, int row, int z, Tile tile) {}
        final List<TileEntry> tiles = new ArrayList<>();
        map.getLayer().forEachTile((col, row, z, tile) -> tiles.add(new TileEntry(col, row, z, tile)));
        tiles.sort((a, b) -> {
            final double ka = a.col() + a.row() + a.z();
            final double kb = b.col() + b.row() + b.z();
            return Double.compare(ka, kb);
        });

        // World rendering with camera applied.
        final java.awt.geom.AffineTransform original = g.getTransform();
        camera.apply(g);

        boolean playerDrawn = false;
        for (final TileEntry te : tiles) {
            final double tileKey = te.col() + te.row() + te.z();
            // Draw player just before the first tile whose key exceeds the player's.
            if (!playerDrawn && tileKey > playerKey) {
                entityRenderer.render(g, player);
                playerDrawn = true;
            }
            tileRenderer.drawTile(g, te.col(), te.row(), te.tile());
        }
        if (!playerDrawn) {
            entityRenderer.render(g, player);
        }

        g.setTransform(original);

        // HUD (screen space).
        g.setColor(HUD_COLOR);
        g.setFont(HUD_FONT);
        final int hudCol = Math.round(player.getPosition().getCol());
        final int hudRow = Math.round(player.getPosition().getRow());
        final Tile under = map.getTopTile(hudCol, hudRow);
        final String mat = under == null ? "-" : under.material().name();
        final String shp = under == null ? "-" : under.shape().name();
        final String hud1 = String.format("FPS: %d  UPS: %d  POS: [%.1f, %.1f, %.1f]",
                fps, ups,
                player.getPosition().getCol(),
                player.getPosition().getRow(),
                player.getPosition().getZ());
        final String hud2 = String.format("CELL: [%d, %d]  MAT: %s  SHAPE: %s",
                hudCol, hudRow, mat, shp);
        final String hud3 = String.format("DBG centre:[%d,%d]  N:[%d,%d]  E:[%d,%d]  S:[%d,%d]  W:[%d,%d]  key:%.0f",
                dbgCellC[0], dbgCellC[1],
                dbgCellN[0], dbgCellN[1],
                dbgCellE[0], dbgCellE[1],
                dbgCellS[0], dbgCellS[1],
                dbgCellW[0], dbgCellW[1],
                playerKey);
        g.drawString(hud1, 10, 20);
        g.drawString(hud2, 10, 38);
        g.drawString(hud3, 10, 56);
    }
}
