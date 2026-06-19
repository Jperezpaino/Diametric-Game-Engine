package es.noa.rad.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import es.noa.rad.entity.Entity;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;
import es.noa.rad.render.animation.AnimationController;

/**
 * Draws an {@link Entity}. If the entity carries an {@link AnimationController}
 * the current frame is blitted with its feet anchored at the entity's world
 * position; otherwise a small red diamond marker is drawn as a placeholder.
 *
 * @since Phase 1 (sprite path added Phase 5c)
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
        final AnimationController controller = entity.getAnimationController();
        final BufferedImage frame = controller == null ? null
                : controller.currentFrame(entity.getFacing());
        if (frame != null) {
            // Anchor the sprite by its bottom-centre on the world position.
            g.drawImage(frame,
                    p.getX() - frame.getWidth()  / 2,
                    p.getY() - frame.getHeight() + MARKER_HALF_HEIGHT,
                    null);
            return;
        }
        renderMarker(g, p);
    }

    private void renderMarker(final Graphics2D g, final ScreenPoint p) {
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
