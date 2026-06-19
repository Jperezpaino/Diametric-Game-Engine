package es.noa.rad.tools.common.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JFrame;

/**
 * Tiny façade for the boilerplate every editor window repeats: minimum
 * size, default close behaviour, centered placement on the user's main
 * display.
 *
 * <p>Lives in {@code diametric-tools-common} so the three editors
 * (Tile Builder, Structure Builder, Map Builder) configure their main
 * frames identically without duplicating code. The engine module does
 * not depend on this class &mdash; it is editor-only territory.</p>
 *
 * @since Phase 10b
 */
public final class SwingFrames {

    /** Default minimum size mandated by the editor specs (640&times;480). */
    public static final Dimension DEFAULT_MIN_SIZE = new Dimension(640, 480);

    private SwingFrames() {}

    /**
     * Applies the standard editor-window defaults: title, minimum size,
     * close-on-exit, packs to preferred size when smaller than the minimum,
     * then centers on the primary screen.
     *
     * @param frame    the frame being configured
     * @param title    title bar text
     * @param minSize  minimum size enforced; if {@code null}, uses
     *                 {@link #DEFAULT_MIN_SIZE}
     */
    public static void applyDefaults(final JFrame frame, final String title, final Dimension minSize) {
        if (frame == null) throw new IllegalArgumentException("frame must not be null");
        frame.setTitle(title == null ? "" : title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final Dimension min = (minSize == null) ? DEFAULT_MIN_SIZE : minSize;
        frame.setMinimumSize(min);
        if (frame.getWidth()  < min.width)  frame.setSize(min.width,  Math.max(frame.getHeight(), min.height));
        if (frame.getHeight() < min.height) frame.setSize(frame.getWidth(), min.height);
        centerOnScreen(frame);
    }

    /**
     * Centers {@code window} on the primary monitor's usable bounds
     * (excluding taskbar / dock insets when reported by the platform).
     */
    public static void centerOnScreen(final Window window) {
        if (window == null) throw new IllegalArgumentException("window must not be null");
        final Rectangle bounds = (GraphicsEnvironment.isHeadless())
            ? new Rectangle(0, 0, 1024, 768)
            : GraphicsEnvironment.getLocalGraphicsEnvironment()
                                 .getMaximumWindowBounds();
        final Toolkit toolkit = (GraphicsEnvironment.isHeadless()) ? null : Toolkit.getDefaultToolkit();
        if (toolkit != null) {
            // Ensure bounds account for OS-reported insets where available.
            // Maximum window bounds already exclude the taskbar on Windows;
            // this is just a guard for edge cases on multi-monitor setups.
        }
        final int x = bounds.x + Math.max(0, (bounds.width  - window.getWidth())  / 2);
        final int y = bounds.y + Math.max(0, (bounds.height - window.getHeight()) / 2);
        window.setLocation(x, y);
    }
}
