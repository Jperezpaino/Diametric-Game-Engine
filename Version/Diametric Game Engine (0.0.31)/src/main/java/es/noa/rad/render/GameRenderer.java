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
import es.noa.rad.map.TileShape;
import es.noa.rad.pathfinding.PathNode;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;
import es.noa.rad.ui.dialog.Dialog;
import es.noa.rad.ui.dialog.DialogManager;

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

    // HP bar (Phase 6c).
    private static final int    HP_BAR_WIDTH    = 160;
    private static final int    HP_BAR_HEIGHT   = 14;
    private static final int    HP_BAR_MARGIN   = 12;
    private static final Color  HP_BAR_BG       = new Color(0, 0, 0, 180);
    private static final Color  HP_BAR_BORDER   = new Color(230, 230, 230);
    private static final Color  HP_BAR_HIGH     = new Color( 60, 200,  80);
    private static final Color  HP_BAR_MID      = new Color(240, 200,  60);
    private static final Color  HP_BAR_LOW      = new Color(220,  60,  60);

    // Dialog box (Phase 6d).
    private static final int   DIALOG_HEIGHT       = 130;
    private static final int   DIALOG_MARGIN       = 16;
    private static final int   DIALOG_PADDING      = 14;
    private static final int   DIALOG_LINE_HEIGHT  = 20;
    private static final Color DIALOG_BG           = new Color(0, 0, 0, 210);
    private static final Color DIALOG_BORDER       = new Color(230, 230, 230);
    private static final Color DIALOG_SPEAKER      = new Color(255, 220, 120);
    private static final Color DIALOG_FOOTER       = new Color(180, 180, 180);
    private static final Font  DIALOG_SPEAKER_FONT = new Font("SansSerif", Font.BOLD,  15);
    private static final Font  DIALOG_BODY_FONT    = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font  DIALOG_FOOTER_FONT  = new Font("SansSerif", Font.ITALIC, 12);

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
                       final List<PathNode> debugPath, final DialogManager dialogs) {
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

        // Debug path overlay (Phase 4c): polyline from the player's actual
        // sub-cell position through each path node's tile centre. If the
        // player is already closer to the next node than the start node is,
        // skip the start node to avoid a visible backtrack.
        if (debugPath != null && !debugPath.isEmpty()) {
            final int[] xs = new int[debugPath.size() + 1];
            final int[] ys = new int[debugPath.size() + 1];
            xs[0] = pScreen.getX();
            ys[0] = pScreen.getY();
            int count = 1;
            final int skipFrom;
            if (debugPath.size() >= 2) {
                final PathNode n0 = debugPath.get(0);
                final PathNode n1 = debugPath.get(1);
                final float d01 = Math.max(Math.abs(n1.col() - n0.col()),
                                           Math.abs(n1.row() - n0.row()));
                final float dp1 = Math.max(Math.abs(n1.col() - pCol),
                                           Math.abs(n1.row() - pRow));
                skipFrom = (dp1 <= d01) ? 1 : 0;
            } else {
                skipFrom = 0;
            }
            for (int i = skipFrom; i < debugPath.size(); i++) {
                final PathNode n = debugPath.get(i);
                final Tile t = map.getTopTile(n.col(), n.row());
                if (t == null) continue;
                final ScreenPoint cN = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.NW);
                final ScreenPoint cE = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.NE);
                final ScreenPoint cS = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.SE);
                final ScreenPoint cW = projection.projectCorner(t, n.col(), n.row(), TileShape.Corner.SW);
                xs[count] = (cN.getX() + cE.getX() + cS.getX() + cW.getX()) / 4;
                ys[count] = (cN.getY() + cE.getY() + cS.getY() + cW.getY()) / 4;
                count++;
            }
            if (count >= 2) {
                final java.awt.Stroke prev = g.getStroke();
                g.setStroke(new java.awt.BasicStroke(3f,
                        java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g.setColor(PATH_OUTLINE);
                g.drawPolyline(xs, ys, count);
                g.setStroke(prev);
            }
            g.setColor(PATH_FILL);
            for (int i = 1; i < count; i++) {
                g.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
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
        final String pathInfo;
        if (pathLen > 0) {
            final PathNode dest = debugPath.get(pathLen - 1);
            pathInfo = String.format("PATH: %d steps -> (%d, %d)", pathLen, dest.col(), dest.row());
        } else {
            pathInfo = "PATH: idle  (press P or click to walk)";
        }
        g.drawString(pathInfo, 10, 74);

        drawHpBar(g, player);

        if (dialogs != null && dialogs.isActive()) {
            drawDialog(g, dialogs);
        }
    }

    /**
     * Phase 6c: draws an HP bar anchored to the top-right corner of the
     * screen. The fill colour shifts from green to yellow to red as HP drops
     * so terrain damage (water, lava) is immediately legible.
     */
    private void drawHpBar(final Graphics2D g, final Player player) {
        final float hp    = player.getHp();
        final float maxHp = player.getMaxHp();
        final float ratio = maxHp <= 0 ? 0f : Math.max(0f, Math.min(1f, hp / maxHp));

        final int x = GameConfig.SCREEN_WIDTH - HP_BAR_WIDTH - HP_BAR_MARGIN;
        final int y = HP_BAR_MARGIN;

        g.setColor(HP_BAR_BG);
        g.fillRect(x, y, HP_BAR_WIDTH, HP_BAR_HEIGHT);

        final int fillWidth = Math.round(HP_BAR_WIDTH * ratio);
        if (fillWidth > 0) {
            final Color fill;
            if (ratio > 0.5f)      fill = HP_BAR_HIGH;
            else if (ratio > 0.25f) fill = HP_BAR_MID;
            else                   fill = HP_BAR_LOW;
            g.setColor(fill);
            g.fillRect(x, y, fillWidth, HP_BAR_HEIGHT);
        }

        g.setColor(HP_BAR_BORDER);
        g.drawRect(x, y, HP_BAR_WIDTH, HP_BAR_HEIGHT);

        final String label = String.format("HP %d / %d",
                Math.round(hp), Math.round(maxHp));
        final java.awt.FontMetrics fm = g.getFontMetrics();
        final int tx = x + (HP_BAR_WIDTH - fm.stringWidth(label)) / 2;
        final int ty = y + HP_BAR_HEIGHT - (HP_BAR_HEIGHT - fm.getAscent()) / 2 - 2;
        g.setColor(HUD_COLOR);
        g.drawString(label, tx, ty);
    }

    /**
     * Phase 6d: paints the active dialog box at the bottom of the screen,
     * with the speaker name highlighted, the current page wrapped to fit
     * the box and a footer hint indicating how to advance / close.
     */
    private void drawDialog(final Graphics2D g, final DialogManager dialogs) {
        final Dialog d = dialogs.active();
        if (d == null) return;

        final int boxX = DIALOG_MARGIN;
        final int boxY = GameConfig.SCREEN_HEIGHT - DIALOG_MARGIN - DIALOG_HEIGHT;
        final int boxW = GameConfig.SCREEN_WIDTH  - 2 * DIALOG_MARGIN;
        final int boxH = DIALOG_HEIGHT;

        g.setColor(DIALOG_BG);
        g.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g.setColor(DIALOG_BORDER);
        g.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);

        g.setFont(DIALOG_SPEAKER_FONT);
        g.setColor(DIALOG_SPEAKER);
        g.drawString(d.speaker(),
                boxX + DIALOG_PADDING,
                boxY + DIALOG_PADDING + g.getFontMetrics().getAscent());

        g.setFont(DIALOG_BODY_FONT);
        g.setColor(HUD_COLOR);
        final int textWidth = boxW - 2 * DIALOG_PADDING;
        final int textTop   = boxY + DIALOG_PADDING + 28;
        final java.awt.FontMetrics bodyFm = g.getFontMetrics();
        final List<String> lines = wrapText(dialogs.currentPage(), bodyFm, textWidth);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i),
                    boxX + DIALOG_PADDING,
                    textTop + i * DIALOG_LINE_HEIGHT);
        }

        g.setFont(DIALOG_FOOTER_FONT);
        g.setColor(DIALOG_FOOTER);
        final String hint = dialogs.isLastPage()
                ? "[SPACE/ENTER] close"
                : "[SPACE/ENTER] next  (" + (dialogs.pageIndex() + 1)
                  + "/" + d.pages().size() + ")";
        final java.awt.FontMetrics hintFm = g.getFontMetrics();
        g.drawString(hint,
                boxX + boxW - DIALOG_PADDING - hintFm.stringWidth(hint),
                boxY + boxH - DIALOG_PADDING);
    }

    /** Greedy word-wrap. Splits on whitespace; does not break long single words. */
    private static List<String> wrapText(final String text,
                                          final java.awt.FontMetrics fm,
                                          final int maxWidth) {
        final List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            out.add("");
            return out;
        }
        final String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (final String word : words) {
            final String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                if (line.length() > 0) out.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) out.add(line.toString());
        return out;
    }
}
