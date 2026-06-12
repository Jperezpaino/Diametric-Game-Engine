package es.noa.rad.render.animation;

import java.awt.image.BufferedImage;

/**
 * Immutable atlas of equally-sized frames laid out in a regular grid.
 *
 * <p>Frames are addressed by {@code (col, row)} where {@code col} grows to
 * the right and {@code row} grows downward, matching the natural layout of a
 * sprite sheet PNG.</p>
 *
 * @since Phase 5a
 */
public final class SpriteSheet {

    private final BufferedImage image;
    private final int frameWidth;
    private final int frameHeight;
    private final int cols;
    private final int rows;

    /**
     * @param image       atlas image
     * @param frameWidth  width of a single frame in pixels
     * @param frameHeight height of a single frame in pixels
     */
    public SpriteSheet(final BufferedImage image, final int frameWidth, final int frameHeight) {
        if (image == null) throw new IllegalArgumentException("image is null");
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("frame size must be positive");
        }
        this.image       = image;
        this.frameWidth  = frameWidth;
        this.frameHeight = frameHeight;
        this.cols = image.getWidth()  / frameWidth;
        this.rows = image.getHeight() / frameHeight;
    }

    public int getFrameWidth()  { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public int getCols()        { return cols; }
    public int getRows()        { return rows; }

    /**
     * Returns the sub-image at {@code (col, row)}.
     *
     * @param col 0-based column
     * @param row 0-based row
     * @return frame view (shares pixel data with the atlas)
     */
    public BufferedImage frame(final int col, final int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) {
            throw new IndexOutOfBoundsException("frame (" + col + "," + row + ") out of sheet");
        }
        return image.getSubimage(col * frameWidth, row * frameHeight, frameWidth, frameHeight);
    }
}
