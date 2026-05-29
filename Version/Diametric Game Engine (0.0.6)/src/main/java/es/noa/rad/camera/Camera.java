package es.noa.rad.camera;

import java.awt.Graphics2D;

import es.noa.rad.config.GameConfig;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;
import es.noa.rad.projection.WorldPoint;

/**
 * Viewport offset that keeps a target world position centred on screen.
 *
 * <p>The camera works directly in screen space: it stores a translation in
 * pixels that is applied to the {@link Graphics2D} before any world drawing
 * takes place.</p>
 *
 * <p>Phase 1 keeps the implementation minimal: no clamping to map bounds,
 * which will be added in Phase 2.</p>
 *
 * @since Phase 1
 */
public final class Camera {

    private float offsetX;
    private float offsetY;

    /**
     * Recalculates the offset so the given target sits in the centre of the
     * screen.
     *
     * @param target world position to follow
     * @param proj   projection used for the conversion
     */
    public void update(final WorldPoint target, final IsoProjection proj) {
        final ScreenPoint targetOnScreen = proj.worldToScreen(target);
        offsetX = (GameConfig.SCREEN_WIDTH / 2f) - targetOnScreen.getX();
        offsetY = (GameConfig.SCREEN_HEIGHT / 2f) - targetOnScreen.getY();
    }

    /**
     * Applies the camera translation to the given graphics context.
     *
     * @param g graphics context
     */
    public void apply(final Graphics2D g) {
        g.translate(Math.round(offsetX), Math.round(offsetY));
    }

    public float getOffsetX() { return offsetX; }
    public float getOffsetY() { return offsetY; }
}
