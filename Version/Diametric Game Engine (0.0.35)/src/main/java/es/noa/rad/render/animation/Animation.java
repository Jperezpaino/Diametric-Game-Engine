package es.noa.rad.render.animation;

import java.awt.image.BufferedImage;

/**
 * A looped sequence of frames taken from a single {@link SpriteSheet} row.
 *
 * <p>Each frame lasts {@code frameDuration} seconds. The animation loops
 * indefinitely; clients pass the cumulative elapsed seconds to {@link
 * #frameAt(double)} and get back the active frame image.</p>
 *
 * @since Phase 5a
 */
public final class Animation {

    private final SpriteSheet sheet;
    private final int         row;
    private final int[]       cols;
    private final double      frameDuration;
    private final double      totalDuration;

    /**
     * @param sheet         source atlas
     * @param row           sheet row to read from
     * @param cols          column indices of the frames, in playback order
     * @param frameDuration seconds per frame (must be {@code > 0})
     */
    public Animation(final SpriteSheet sheet, final int row, final int[] cols,
                     final double frameDuration) {
        if (sheet == null) throw new IllegalArgumentException("sheet is null");
        if (cols == null || cols.length == 0) {
            throw new IllegalArgumentException("cols must contain at least one frame");
        }
        if (frameDuration <= 0) {
            throw new IllegalArgumentException("frameDuration must be > 0");
        }
        this.sheet         = sheet;
        this.row           = row;
        this.cols          = cols.clone();
        this.frameDuration = frameDuration;
        this.totalDuration = frameDuration * cols.length;
    }

    public int frameCount() { return cols.length; }
    public double totalDuration() { return totalDuration; }

    /**
     * @param elapsedSeconds total elapsed time since the animation started
     * @return the frame image active at that instant
     */
    public BufferedImage frameAt(final double elapsedSeconds) {
        final double wrapped = elapsedSeconds % totalDuration;
        final int index = Math.min(cols.length - 1, (int) (wrapped / frameDuration));
        return sheet.frame(cols[index], row);
    }
}
