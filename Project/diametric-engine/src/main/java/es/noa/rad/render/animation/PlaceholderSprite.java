package es.noa.rad.render.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import es.noa.rad.map.Direction8;

/**
 * Builds a programmatic placeholder sprite sheet so the animation pipeline
 * can be exercised before real artwork is available.
 *
 * <p>Layout: 8 rows (one per {@link Direction8} value, in enum order) ×
 * 3 columns (frame 0 = idle, frames 1 and 2 = walk bounce). Each frame
 * shows a coloured body (hue derived from the direction) and a white arrow
 * pointing in the screen-space heading.</p>
 *
 * @since Phase 5c
 */
public final class PlaceholderSprite {

    /** Width of a placeholder frame in pixels. */
    public static final int FRAME_WIDTH  = 32;
    /** Height of a placeholder frame in pixels. */
    public static final int FRAME_HEIGHT = 48;

    private static final int COL_IDLE  = 0;
    private static final int COL_WALK1 = 1;
    private static final int COL_WALK2 = 2;
    private static final int COLS = 3;

    /** Seconds per walk frame. */
    private static final double WALK_FRAME_DURATION = 0.18;
    /** Seconds per idle frame (only one frame ??? duration is irrelevant but valid). */
    private static final double IDLE_FRAME_DURATION = 1.0;

    private PlaceholderSprite() {}

    /** Builds the placeholder sheet and a controller bound to every state/direction. */
    public static AnimationController createController() {
        final SpriteSheet sheet = buildSheet();
        final AnimationController controller = new AnimationController();
        final Direction8[] dirs = Direction8.values();
        for (int i = 0; i < dirs.length; i++) {
            final Direction8 d = dirs[i];
            controller.bind(AnimationState.IDLE, d,
                    new Animation(sheet, i, new int[] { COL_IDLE }, IDLE_FRAME_DURATION));
            controller.bind(AnimationState.WALK, d,
                    new Animation(sheet, i, new int[] { COL_WALK1, COL_WALK2 }, WALK_FRAME_DURATION));
        }
        return controller;
    }

    private static SpriteSheet buildSheet() {
        final Direction8[] dirs = Direction8.values();
        final BufferedImage img = new BufferedImage(
                FRAME_WIDTH * COLS, FRAME_HEIGHT * dirs.length, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int r = 0; r < dirs.length; r++) {
                final Color body = Color.getHSBColor((float) r / dirs.length, 0.75f, 0.95f);
                drawFrame(g, COL_IDLE  * FRAME_WIDTH, r * FRAME_HEIGHT, body, dirs[r], 0);
                drawFrame(g, COL_WALK1 * FRAME_WIDTH, r * FRAME_HEIGHT, body, dirs[r], -2);
                drawFrame(g, COL_WALK2 * FRAME_WIDTH, r * FRAME_HEIGHT, body, dirs[r], +2);
            }
        } finally {
            g.dispose();
        }
        return new SpriteSheet(img, FRAME_WIDTH, FRAME_HEIGHT);
    }

    private static void drawFrame(final Graphics2D g, final int x0, final int y0,
                                  final Color body, final Direction8 dir, final int bobY) {
        // Feet shadow.
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x0 + 6, y0 + FRAME_HEIGHT - 8, FRAME_WIDTH - 12, 6);

        // Body: rounded torso + head.
        final int cx = x0 + FRAME_WIDTH / 2;
        final int torsoTop = y0 + 18 + bobY;
        final int torsoH   = 20;
        final int torsoW   = 16;
        g.setColor(body);
        g.fillRoundRect(cx - torsoW / 2, torsoTop, torsoW, torsoH, 8, 8);
        g.setColor(body.darker());
        g.drawRoundRect(cx - torsoW / 2, torsoTop, torsoW, torsoH, 8, 8);

        final int headR = 8;
        g.setColor(body.brighter());
        g.fillOval(cx - headR, torsoTop - headR * 2 + 2, headR * 2, headR * 2);
        g.setColor(body.darker());
        g.drawOval(cx - headR, torsoTop - headR * 2 + 2, headR * 2, headR * 2);

        // Facing arrow ??? screen-space heading derived from world delta.
        final int sx = dir.dCol() - dir.dRow();          // ??? -2..+2
        final int sy = dir.dCol() + dir.dRow();          // ??? -2..+2
        final int len = 10;
        final double norm = Math.hypot(sx, sy);
        final double ux = sx / norm;
        final double uy = sy / norm;
        final int tipX = cx + (int) Math.round(ux * len);
        final int tipY = torsoTop + torsoH / 2 + (int) Math.round(uy * len);
        final Polygon arrow = new Polygon();
        // Perpendicular for arrow base.
        final double px = -uy;
        final double py =  ux;
        arrow.addPoint(tipX, tipY);
        arrow.addPoint(cx + (int) Math.round(px * 4), torsoTop + torsoH / 2 + (int) Math.round(py * 4));
        arrow.addPoint(cx - (int) Math.round(px * 4), torsoTop + torsoH / 2 - (int) Math.round(py * 4));
        g.setColor(Color.WHITE);
        g.fillPolygon(arrow);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1f));
        g.drawPolygon(arrow);
    }
}
