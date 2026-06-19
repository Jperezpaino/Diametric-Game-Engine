package es.noa.rad.render.animation;

import java.awt.image.BufferedImage;

import es.noa.rad.map.Direction8;
import es.noa.rad.render.AssetImages;

/**
 * Loader of entity sprite atlases from PNG.
 *
 * <p>Convention: classpath resource {@code entities/<name>.png} laid out as a
 * grid mirroring {@link PlaceholderSprite}:</p>
 * <ul>
 *   <li>Rows: one per {@link Direction8} value in enum order.</li>
 *   <li>Columns: {@code 0 = IDLE}, {@code 1 = WALK frame 1}, {@code 2 = WALK frame 2}.</li>
 *   <li>Frame size: {@link PlaceholderSprite#FRAME_WIDTH} × {@link PlaceholderSprite#FRAME_HEIGHT}
 *       (defaults to 32×48, matching the placeholder so {@link es.noa.rad.render.EntityRenderer}
 *       anchor math stays untouched).</li>
 *   <li>Magenta {@code RGB(255,0,255)} pixels are converted to fully
 *       transparent during load, see {@link AssetImages}.</li>
 * </ul>
 *
 * <p>The PNG must be exactly
 * {@code (FRAME_WIDTH * 3) × (FRAME_HEIGHT * 8) = 96 × 384} pixels. If the
 * resource is missing, unreadable or has the wrong dimensions, the loader
 * returns {@code null} and callers are expected to fall back to
 * {@link PlaceholderSprite#createController()}.</p>
 *
 * @since Phase 7c
 */
public final class EntitySprites {

    private static final int COL_IDLE  = 0;
    private static final int COL_WALK1 = 1;
    private static final int COL_WALK2 = 2;
    private static final int COLS      = 3;

    /** Same cadence as the placeholder so behaviour is indistinguishable when swapping art in. */
    private static final double WALK_FRAME_DURATION = 0.18;
    private static final double IDLE_FRAME_DURATION = 1.0;

    private EntitySprites() {}

    /**
     * @param name entity key (e.g. {@code "player"})
     * @return an {@link AnimationController} bound to every {@code (state, dir)}
     *         pair, or {@code null} if the PNG is missing/invalid
     */
    public static AnimationController tryLoadController(final String name) {
        final BufferedImage atlas = AssetImages.tryLoad("entities/" + name + ".png");
        if (atlas == null) return null;

        final int fw = PlaceholderSprite.FRAME_WIDTH;
        final int fh = PlaceholderSprite.FRAME_HEIGHT;
        final Direction8[] dirs = Direction8.values();
        final int expectedW = fw * COLS;
        final int expectedH = fh * dirs.length;
        if (atlas.getWidth() != expectedW || atlas.getHeight() != expectedH) {
            System.err.println("[EntitySprites] '" + name + ".png' is "
                    + atlas.getWidth() + "x" + atlas.getHeight()
                    + " but expected " + expectedW + "x" + expectedH + " — ignoring.");
            return null;
        }

        final SpriteSheet sheet = new SpriteSheet(atlas, fw, fh);
        final AnimationController controller = new AnimationController();
        for (int i = 0; i < dirs.length; i++) {
            final Direction8 d = dirs[i];
            controller.bind(AnimationState.IDLE, d,
                    new Animation(sheet, i, new int[] { COL_IDLE }, IDLE_FRAME_DURATION));
            controller.bind(AnimationState.WALK, d,
                    new Animation(sheet, i, new int[] { COL_WALK1, COL_WALK2 }, WALK_FRAME_DURATION));
        }
        return controller;
    }
}
