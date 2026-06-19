package es.noa.rad.tools.structurebuilder;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import es.noa.rad.tools.common.swing.SwingFrames;

/**
 * Phase 10c skeleton entry point for the Structure Builder editor (UI-002).
 *
 * <p>Opens an empty centered 640×480 frame to validate the multi-module
 * wiring. Real UI for authoring {@code Structure} resources lands in
 * Phase 12.</p>
 */
public final class StructureBuilderApp {

    private StructureBuilderApp() {}

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(StructureBuilderApp::launch);
    }

    private static void launch() {
        final JFrame frame = new JFrame();
        frame.setSize(SwingFrames.DEFAULT_MIN_SIZE);
        frame.add(new JLabel(
            "<html><center><h2>Structure Builder</h2>"
          + "<p>Phase 10c module skeleton.</p>"
          + "<p>UI-002 implementation lands in Phase 12.</p></center></html>",
            SwingConstants.CENTER));
        SwingFrames.applyDefaults(frame, "Structure Builder", null);
        frame.setVisible(true);
    }
}
