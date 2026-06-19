package es.noa.rad.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Shared image loader for PNG assets used by every renderer.
 *
 * <p>Every load goes through the same two-step pipeline:</p>
 * <ol>
 *   <li>Resolve the classpath resource via the context class loader.</li>
 *   <li>Convert the decoded image to ARGB and replace every pixel matching
 *       the magenta colour key {@code RGB(255,0,255)} with full transparency
 *       so artists can author sprites without an alpha channel.</li>
 * </ol>
 *
 * <p>Returns {@code null} when the resource is missing or unreadable so the
 * caller can fall back to a placeholder.</p>
 *
 * @since Phase 7c
 */
public final class AssetImages {

    /** Color key treated as transparent when present in a sprite PNG (magenta). */
    public static final int COLOR_KEY_RGB = 0x00FF00FF;

    private AssetImages() {}

    /** Loads {@code classpath} as an ARGB image with magenta keying applied, or {@code null}. */
    public static BufferedImage tryLoad(final String classpath) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(classpath)) {
            if (in == null) return null;
            final BufferedImage raw = ImageIO.read(in);
            return raw == null ? null : applyMagentaKey(raw);
        } catch (IOException ex) {
            System.err.println("[AssetImages] failed to read " + classpath + ": " + ex.getMessage());
            return null;
        }
    }

    private static BufferedImage applyMagentaKey(final BufferedImage src) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = out.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        final int keyRgb = COLOR_KEY_RGB & 0x00FFFFFF;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((out.getRGB(x, y) & 0x00FFFFFF) == keyRgb) {
                    out.setRGB(x, y, 0x00000000);
                }
            }
        }
        return out;
    }
}
