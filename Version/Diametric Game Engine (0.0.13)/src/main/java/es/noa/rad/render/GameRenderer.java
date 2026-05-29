package es.noa.rad.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import es.noa.rad.camera.Camera;
import es.noa.rad.config.GameConfig;
import es.noa.rad.entity.Player;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileShape;
import es.noa.rad.pathfinding.PathNode;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;

/**
 * High-level renderer that orchestrates every drawing step of a frame.
 *
 * <p>Responsibilities:</p>
 * <ol>
 *   <li>Clear the back buffer.</li>
 *   <li>Collect all tiles and sort them with the <em>painter's algorithm</em>
 *       (ascending {@code col + row + z}) for correct 2:1 isometric depth.</li>
 *   <li>Determine the player's painter key via 5-point diamond sampling:
 *       project the centre and the four cardinal extremes of the marker
 *       diamond to screen space, unproject each back to a world cell at the
 *       player's z, and use the highest {@code col + row} found.  This
 *       guarantees every tile visually in front of the player is drawn after
 *       it.</li>
 *   <li>Apply the camera translation and interleave the player into the
 *       sorted tile list at the correct depth slot.</li>
 *   <li>Draw the HUD in screen space (FPS, UPS, position, cell under foot).</li>
 * </ol>
 *
 * @since Phase 1  (painter's algorithm + player depth sorting: Phase 3 debt-close)
 */
public final class GameRenderer {

    private static final Color BACKGROUND = new Color(20, 20, 30);
    private static final Color HUD_COLOR  = Color.WHITE;
    private static final Color PATH_FILL    = new Color(255, 230, 60, 110);
    private static final Color PATH_OUTLINE = new Color(255, 200, 0, 200);
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
                       final Camera camera, final int fps, final int ups,
                       final List<PathNode> debugPath) {
        // Clear background.
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Painter's key for the player ---
        // Sample 5 points of the marker diamond (centre + N/E/S/W extremes),
        // unproject each to a world cell at the player's z, and take the
        // southernmost cell (highest col+row).  This ensures every tile
        // visually in front of the player is drawn after it.
        final float pCol = player.getPosition().getCol();
        final float pRow = player.getPosition().getRow();
        final float pZ   = player.getPosition().getZ();

        final ScreenPoint pScreen = projection.worldToScreen(pCol, pRow, pZ);
        final float halfTileW = GameConfig.TILE_WIDTH  / 2f;
        final float halfTileH = GameConfig.TILE_HEIGHT / 2f;

        final int hw = EntityRenderer.MARKER_HALF_WIDTH;
        final int hh = EntityRenderer.MARKER_HALF_HEIGHT;
        // {dx, dy} screen-pixel offsets: centre, N, E, S, W extremes.
        final int[][] offsets = { {0, 0}, {0, -hh}, {hw, 0}, {0, hh}, {-hw, 0} };

        int maxCellSum = Integer.MIN_VALUE;
        for (final int[] off : offsets) {
            final int sx = pScreen.getX() + off[0];
            final int sy = pScreen.getY() + off[1];
            final float cr  = (sy + pZ * GameConfig.Z_STEP_PX) / halfTileH;
            final float cmr = sx / halfTileW;
            final int ic = Math.round((cr + cmr) / 2.0f);
            final int ir = Math.round((cr - cmr) / 2.0f);
            maxCellSum = Math.max(maxCellSum, ic + ir);
        }
        final double playerKey = maxCellSum + pZ;

        // --- Gather and sort tiles (painter's algorithm) ---
        record TileEntry(int col, int row, int z, Tile tile) {}
        final List<TileEntry> tiles = new ArrayList<>();
        map.getLayer().forEachTile((col, row, z, tile) -> tiles.add(new TileEntry(col, row, z, tile)));
        tiles.sort((a, b) -> Double.compare(a.col() + a.row() + a.z(),
                                            b.col() + b.row() + b.z()));

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

        // Debug path overlay (Phase 4c): translucent diamond on each path cell.
        if (debugPath != null && !debugPath.isEmpty()) {
            for (final PathNode n : debugPath) {
                final Tile t = map.getTopTile(n.col(), n.row());
                if (t == null) continue;
                final ScreenPoint cN = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.NW);
                final ScreenPoint cE = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.NE);
                final ScreenPoint cS = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.SE);
                final ScreenPoint cW = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.SW);
                final Polygon diamond = new Polygon();
                diamond.addPoint(cN.getX(), cN.getY());
                diamond.addPoint(cE.getX(), cE.getY());
                diamond.addPoint(cS.getX(), cS.getY());
                diamond.addPoint(cW.getX(), cW.getY());
                g.setColor(PATH_FILL);
                g.fillPolygon(diamond);
                g.setColor(PATH_OUTLINE);
                g.drawPolygon(diamond);
            }
        }

        g.setTransform(original);

        // --- HUD (screen space) ---
        g.setColor(HUD_COLOR);
        g.setFont(HUD_FONT);
        final int hudCol = Math.round(pCol);
        final int hudRow = Math.round(pRow);
        final Tile under = map.getTopTile(hudCol, hudRow);
        final String mat = under == null ? "-" : under.material().name();
        final String shp = under == null ? "-" : under.shape().name();
        g.drawString(String.format("FPS: %d  UPS: %d  POS: [%.1f, %.1f, %.1f]",
                fps, ups, pCol, pRow, pZ), 10, 20);
        g.drawString(String.format("CELL: [%d, %d]  MAT: %s  SHAPE: %s",
                hudCol, hudRow, mat, shp), 10, 38);
        g.drawString(String.format("CAM: %-6s  off:[%.0f, %.0f]  zoom:%.2gx",
                camera.getMode(), camera.getOffsetX(), camera.getOffsetY(),
                camera.getZoom()), 10, 56);
        final int pathLen = debugPath == null ? 0 : debugPath.size();
        g.drawString(String.format("PATH: %d steps  (press P to recompute)", pathLen), 10, 74);
    }
}
