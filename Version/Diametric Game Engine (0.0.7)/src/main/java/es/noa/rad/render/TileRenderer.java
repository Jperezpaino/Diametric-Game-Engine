package es.noa.rad.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileShape;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;

/**
 * Renders every tile of a {@link TileMap} as a diamond polygon, with proper
 * back-to-front ordering and side faces for elevated tiles.
 *
 * <h3>Painter''s algorithm with elevation</h3>
 * <p>{@link TileLayer#forEachTile} visits voxels in row-outer / col-inner /
 * z-ascending order, which is exactly the painter''s order needed for the
 * 2:1 diametric projection.</p>
 *
 * @since Phase 1 (voxel rewrite Phase 3)
 */
public final class TileRenderer {

    private static final Color OUTLINE      = new Color(0, 0, 0, 60);
    private static final Color SIDE_OUTLINE = new Color(0, 0, 0, 90);

    private final IsoProjection projection;

    public TileRenderer(final IsoProjection projection) {
        this.projection = projection;
    }

    /** Renders every voxel in the map. */
    public void render(final Graphics2D g, final TileMap map) {
        map.getLayer().forEachTile((col, row, z, tile) -> drawTile(g, col, row, tile));
    }

    // ------------------------------------------------------------------------
    // Per-tile rendering
    // ------------------------------------------------------------------------

    private void drawTile(final Graphics2D g, final int col, final int row, final Tile tile) {
        final Color base = tile.material().getColor();

        // Project the four diamond corners of the top face.
        final ScreenPoint cN = projection.projectCorner(tile, col, row, TileShape.Corner.NW);
        final ScreenPoint cE = projection.projectCorner(tile, col, row, TileShape.Corner.NE);
        final ScreenPoint cS = projection.projectCorner(tile, col, row, TileShape.Corner.SE);
        final ScreenPoint cW = projection.projectCorner(tile, col, row, TileShape.Corner.SW);

        // Side faces under SE and SW edges (the two facing the camera).
        // The bottom of the side face sits at world z = 0 (or the tile''s base
        // elevation if the tile is part of a stack).
        final int baseZ = 0;
        if (tile.elevation() > baseZ
                || tile.shape().edgeHeight(es.noa.rad.map.EdgeSide.EAST)  > 0
                || tile.shape().edgeHeight(es.noa.rad.map.EdgeSide.SOUTH) > 0
                || tile.shape().edgeHeight(es.noa.rad.map.EdgeSide.WEST)  > 0) {
            // East face (between NE and SE corners).
            drawSideFace(g, cE, cS, col, row, +0.5f, -0.5f, +0.5f, +0.5f, baseZ, base, 0.75f);
            // South face (between SE and SW corners).
            drawSideFace(g, cS, cW, col, row, +0.5f, +0.5f, -0.5f, +0.5f, baseZ, base, 0.55f);
        }

        drawTopQuad(g, cN, cE, cS, cW, base);
    }

    private void drawSideFace(final Graphics2D g,
                              final ScreenPoint topA, final ScreenPoint topB,
                              final int col, final int row,
                              final float aColOff, final float aRowOff,
                              final float bColOff, final float bRowOff,
                              final int baseZ,
                              final Color base, final float factor) {
        final ScreenPoint botA = projection.worldToScreen(col + aColOff, row + aRowOff, baseZ);
        final ScreenPoint botB = projection.worldToScreen(col + bColOff, row + bRowOff, baseZ);

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
                             final ScreenPoint nw, final ScreenPoint ne,
                             final ScreenPoint se, final ScreenPoint sw,
                             final Color fill) {
        final Polygon top = new Polygon();
        top.addPoint(nw.getX(), nw.getY());
        top.addPoint(ne.getX(), ne.getY());
        top.addPoint(se.getX(), se.getY());
        top.addPoint(sw.getX(), sw.getY());

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