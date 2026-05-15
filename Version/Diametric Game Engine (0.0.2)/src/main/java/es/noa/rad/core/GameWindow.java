package es.noa.rad.core;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import es.noa.rad.config.GameConfig;

/**
 * Wraps a {@link JFrame} and a {@link Canvas} with a {@link BufferStrategy}
 * (double-buffered) used by the engine to render frames.
 *
 * @since Phase 1
 */
public final class GameWindow {

    private final JFrame frame;
    private final Canvas canvas;
    private BufferStrategy bufferStrategy;

    /** Creates and shows the game window. */
    public GameWindow() {
        this.canvas = new Canvas();
        final Dimension size = new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        canvas.setPreferredSize(size);
        canvas.setMinimumSize(size);
        canvas.setMaximumSize(size);
        canvas.setFocusable(true);

        this.frame = new JFrame(GameConfig.WINDOW_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        canvas.createBufferStrategy(2);
        this.bufferStrategy = canvas.getBufferStrategy();
        canvas.requestFocusInWindow();
    }

    /** @return graphics context of the current back buffer. */
    public Graphics2D getGraphics2D() {
        if (bufferStrategy == null) {
            bufferStrategy = canvas.getBufferStrategy();
        }
        return (Graphics2D) bufferStrategy.getDrawGraphics();
    }

    /** Shows the back buffer. */
    public void show() {
        if (bufferStrategy != null && !bufferStrategy.contentsLost()) {
            bufferStrategy.show();
        }
    }

    public Canvas getCanvas() { return canvas; }
}
