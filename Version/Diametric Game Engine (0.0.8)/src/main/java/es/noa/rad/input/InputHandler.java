package es.noa.rad.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * AWT {@link KeyAdapter} that forwards key events to {@link InputState}.
 *
 * <p>Register an instance of this class on the game {@code Canvas} once during
 * engine initialisation. All actual state tracking is delegated to the
 * {@link InputState} it receives.</p>
 *
 * <pre>{@code
 *   InputState state = new InputState();
 *   canvas.addKeyListener(new InputHandler(state));
 * }</pre>
 *
 * @since Phase 2
 */
public final class InputHandler extends KeyAdapter {

    private final InputState state;

    /**
     * @param state shared input snapshot to update
     */
    public InputHandler(final InputState state) {
        this.state = state;
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        state.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        state.keyReleased(e.getKeyCode());
    }
}
