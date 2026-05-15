package es.noa.rad.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import es.noa.rad.config.GameConfig;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;
import es.noa.rad.map.TileMap;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;

/**
 * Renders every tile of a {@link TileMap} as a diamond polygon.
 *
 * <p><b>Painter's algorithm:</b> tiles are drawn iterating row in the outer
 * loop and column in the inner loop. This produces a back-to-front order in
 * 2:1 diametric projection, which guarantees correct overlap when neighbour
 * tiles share screen pixels.</p>
 *
 * @since Phase 1
 */
public final class TileRenderer {

    private static final Color OUTLINE = new Color(0, 0, 0, 60);

    private final IsoProjection projection;

    public TileRenderer(final IsoProjection projection) {
        this.projection = projection;
    }

    /**
     * Renders the visual layer of the map.
     *
     * @param g   graphics context (camera transform must already be applied)
     * @param map tile map to draw
     */
    public void render(final Graphics2D g, final TileMap map) {
        final TileLayer layer = map.getVisualLayer();
        final int halfW = GameConfig.TILE_WIDTH / 2;
        final int halfH = GameConfig.TILE_HEIGHT / 2;

        // Painter's algorithm: row outer, col inner = back-to-front order.
        for (int row = 0; row < layer.getDepth(); row++) {
            for (int col = 0; col < layer.getWidth(); col++) {
                final Tile tile = layer.getTile(col, row);
                if (tile == null) {
                    continue;
                }
                final ScreenPoint p = projection.worldToScreen(col, row, 0);
                drawDiamond(g, p.getX(), p.getY(), halfW, halfH, tile.getType().getColor());
            }
        }
    }

    private void drawDiamond(final Graphics2D g, final int cx, final int cy,
                             final int halfW, final int halfH, final Color fill) {
        final Polygon diamond = new Polygon();
        diamond.addPoint(cx,           cy - halfH); // top
        diamond.addPoint(cx + halfW,   cy);         // right
        diamond.addPoint(cx,           cy + halfH); // bottom
        diamond.addPoint(cx - halfW,   cy);         // left

        g.setColor(fill);
        g.fillPolygon(diamond);
        g.setColor(OUTLINE);
        g.drawPolygon(diamond);
    }
}
