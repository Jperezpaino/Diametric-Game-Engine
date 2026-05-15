package es.noa.rad.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import es.noa.rad.config.GameConfig;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileType;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;

/**
 * Renders every tile of a {@link TileMap} as a diamond polygon, with proper
 * back-to-front ordering and side faces for elevated tiles.
 *
 * <h3>Painter's algorithm with elevation</h3>
 * <p>Tiles are still iterated row-outer / col-inner, but each tile is
 * projected through {@link IsoProjection#worldToScreen(Tile, int, int)},
 * which already subtracts {@code z * TILE_HEIGHT} from the screen Y.
 * Because elevated tiles are drawn <em>later</em> within the same
 * (col,row) iteration order, they correctly cover the side faces of
 * neighbours behind them.</p>
 *
 * <h3>Side faces</h3>
 * <p>For each tile with {@code elevation &gt; 0} two quadrilaterals are drawn
 * underneath the top diamond: the south-west and south-east faces. They use a
 * darkened tint of the tile colour to fake volume.</p>
 *
 * @since Phase 1 (elevation rendering added Phase 3)
 */
public final class TileRenderer {

    private static final Color OUTLINE      = new Color(0, 0, 0, 60);
    private static final Color SIDE_OUTLINE = new Color(0, 0, 0, 90);

    private final IsoProjection projection;

    public TileRenderer(final IsoProjection projection) {
        this.projection = projection;
    }

    /**
     * Renders the visual layer (and the object layer on top, if any).
     *
     * @param g   graphics context (camera transform must already be applied)
     * @param map tile map to draw
     */
    public void render(final Graphics2D g, final TileMap map) {
        final TileLayer visual  = map.getVisualLayer();
        final TileLayer objects = map.getObjectLayer();
        final int halfW = GameConfig.TILE_WIDTH / 2;
        final int halfH = GameConfig.TILE_HEIGHT / 2;

        for (int row = 0; row < visual.getDepth(); row++) {
            for (int col = 0; col < visual.getWidth(); col++) {
                drawCell(g, visual,  col, row, halfW, halfH);
                drawCell(g, objects, col, row, halfW, halfH);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Per-cell rendering
    // -------------------------------------------------------------------------

    private void drawCell(final Graphics2D g, final TileLayer layer,
                          final int col, final int row,
                          final int halfW, final int halfH) {
        final Tile tile = layer.getTile(col, row);
        if (tile == null) {
            return;
        }
        final Color base = tile.getType().getColor();

        // Project the four diamond corners individually, each with its own
        // absolute height (tile elevation + corner offset). For flat tiles all
        // four heights coincide and the result is a regular diamond; for ramps
        // the polygon ends up tilted, exposing the slope.
        final int[] heights = tile.getType().getCornerHeights();
        final int e = tile.getElevation();

        final ScreenPoint cN = projection.worldToScreen(col - 0.5f, row - 0.5f, e + heights[TileType.N]);
        final ScreenPoint cE = projection.worldToScreen(col + 0.5f, row - 0.5f, e + heights[TileType.E]);
        final ScreenPoint cS = projection.worldToScreen(col + 0.5f, row + 0.5f, e + heights[TileType.S]);
        final ScreenPoint cW = projection.worldToScreen(col - 0.5f, row + 0.5f, e + heights[TileType.W]);

        // Side faces under SE and SW edges (the two edges that face the camera).
        // Each face is a quad: top edge follows the actual corner heights,
        // bottom edge sits at world z = 0.
        if (e + heights[TileType.E] > 0 || e + heights[TileType.S] > 0
                || e + heights[TileType.W] > 0) {
            drawSideFace(g, cE, cS, col, row, +0.5f, -0.5f, +0.5f, +0.5f, base, 0.75f);
            drawSideFace(g, cS, cW, col, row, +0.5f, +0.5f, -0.5f, +0.5f, base, 0.55f);
        }

        drawTopQuad(g, cN, cE, cS, cW, base);
    }

    /**
     * Draws a vertical quad whose top edge connects two already-projected
     * corners and whose bottom edge sits at z = 0 in world space.
     */
    private void drawSideFace(final Graphics2D g,
                              final ScreenPoint topA, final ScreenPoint topB,
                              final int col, final int row,
                              final float aColOff, final float aRowOff,
                              final float bColOff, final float bRowOff,
                              final Color base, final float factor) {
        final ScreenPoint botA = projection.worldToScreen(col + aColOff, row + aRowOff, 0f);
        final ScreenPoint botB = projection.worldToScreen(col + bColOff, row + bRowOff, 0f);

        final Polygon face = new Polygon();
        face.addPoint(topA.getX(), topA.getY());
        face.addPoint(topB.getX(), topB.getY());
        face.addPoint(botB.getX(), botB.getY());
        face.addPoint(botA.getX(), botA.getY());

        g.setColor(darken(base, factor));
        g.fillPolygon(face);
        g.setColor(SIDE_OUTLINE);
        g.drawPolygon(face);
    }

    private void drawTopQuad(final Graphics2D g,
                             final ScreenPoint n, final ScreenPoint e,
                             final ScreenPoint s, final ScreenPoint w,
                             final Color fill) {
        final Polygon top = new Polygon();
        top.addPoint(n.getX(), n.getY());
        top.addPoint(e.getX(), e.getY());
        top.addPoint(s.getX(), s.getY());
        top.addPoint(w.getX(), w.getY());

        g.setColor(fill);
        g.fillPolygon(top);
        g.setColor(OUTLINE);
        g.drawPolygon(top);
    }

    private static Color darken(final Color c, final float factor) {
        return new Color(
                Math.round(c.getRed()   * factor),
                Math.round(c.getGreen() * factor),
                Math.round(c.getBlue()  * factor));
    }
}
