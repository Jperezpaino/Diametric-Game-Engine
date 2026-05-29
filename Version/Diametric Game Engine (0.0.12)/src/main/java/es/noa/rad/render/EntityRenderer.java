package es.noa.rad.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import es.noa.rad.entity.Entity;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;

/**
 * Draws an {@link Entity} as a small red diamond on top of the map.
 *
 * <p>This is the placeholder representation used while the engine has no real
 * sprites. The marker is 20 px wide and 10 px tall, keeping the 2:1 ratio.</p>
 *
 * @since Phase 1
 */
public final class EntityRenderer {

    public static final int MARKER_HALF_WIDTH  = 10;
    public static final int MARKER_HALF_HEIGHT = 5;

    private static final Color FILL = new Color(220, 50, 50);
    private static final Color OUTLINE = new Color(80, 0, 0);

    private final IsoProjection projection;

    public EntityRenderer(final IsoProjection projection) {
        this.projection = projection;
    }

    /**
     * Renders an entity at its current world position.
     *
     * @param g      graphics context (camera transform already applied)
     * @param entity entity to draw
     */
    public void render(final Graphics2D g, final Entity entity) {
        final ScreenPoint p = projection.worldToScreen(entity.getPosition());

        final Polygon marker = new Polygon();
        marker.addPoint(p.getX(),                       p.getY() - MARKER_HALF_HEIGHT);
        marker.addPoint(p.getX() + MARKER_HALF_WIDTH,   p.getY());
        marker.addPoint(p.getX(),                       p.getY() + MARKER_HALF_HEIGHT);
        marker.addPoint(p.getX() - MARKER_HALF_WIDTH,   p.getY());

        g.setColor(FILL);
        g.fillPolygon(marker);
        g.setColor(OUTLINE);
        g.drawPolygon(marker);
    }
}
