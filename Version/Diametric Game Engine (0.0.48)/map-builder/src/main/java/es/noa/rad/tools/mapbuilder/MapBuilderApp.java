package es.noa.rad.tools.mapbuilder;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import es.noa.rad.tools.common.swing.SwingFrames;

/**
 * Phase 10c skeleton entry point for the Map Builder editor (UI-003).
 *
 * <p>Opens an empty centered 640×480 frame to validate the multi-module
 * wiring. Real UI for placing {@code TileInstance}s and
 * {@code StructureInstance}s on a map lands in Phase 13.</p>
 */
public final class MapBuilderApp {

    private MapBuilderApp() {}

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(MapBuilderApp::launch);
    }

    private static void launch() {
        final JFrame frame = new JFrame();
        frame.setSize(SwingFrames.DEFAULT_MIN_SIZE);
        frame.add(new JLabel(
            "<html><center><h2>Map Builder</h2>"
          + "<p>Phase 10c module skeleton.</p>"
          + "<p>UI-003 implementation lands in Phase 13.</p></center></html>",
            SwingConstants.CENTER));
        SwingFrames.applyDefaults(frame, "Map Builder", null);
        frame.setVisible(true);
    }
}
