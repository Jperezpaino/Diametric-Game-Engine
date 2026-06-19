package es.noa.rad.tools.tilebuilder;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import es.noa.rad.tools.common.swing.SwingFrames;

/**
 * Phase 10c skeleton entry point for the Tile Builder editor (UI-001).
 *
 * <p>Opens an empty centered 640×480 frame using
 * {@link SwingFrames#applyDefaults(JFrame, String, java.awt.Dimension)} so
 * the multi-module wiring is exercised end-to-end (this module &rarr;
 * {@code diametric-tools-common} &rarr; {@code diametric-engine}). The
 * actual editor UI lands progressively in Phase 11.</p>
 */
public final class TileBuilderApp {

    private TileBuilderApp() {}

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(TileBuilderApp::launch);
    }

    private static void launch() {
        final JFrame frame = new JFrame();
        frame.setSize(SwingFrames.DEFAULT_MIN_SIZE);
        frame.add(new JLabel(
            "<html><center><h2>Tile Builder</h2>"
          + "<p>Phase 10c module skeleton.</p>"
          + "<p>UI-001 implementation lands in Phase 11.</p></center></html>",
            SwingConstants.CENTER));
        SwingFrames.applyDefaults(frame, "Tile Builder", null);
        frame.setVisible(true);
    }
}
