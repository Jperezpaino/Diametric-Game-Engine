package es.noa.rad.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * AWT {@link MouseAdapter} that captures the last unconsumed mouse click on
 * the game canvas. The game loop polls {@link #consume()} once per tick to
 * drain the pending click. Newer clicks always overwrite older ones.
 *
 * @since Phase 4d
 */
public final class MouseInput extends MouseAdapter {

    /** Logical mouse button. */
    public enum Button { LEFT, RIGHT, OTHER }

    /** Single pending click event. */
    public record Click(Button button, int x, int y) { }

    private Click pending;

    @Override
    public synchronized void mousePressed(final MouseEvent e) {
        final Button b;
        switch (e.getButton()) {
            case MouseEvent.BUTTON1 -> b = Button.LEFT;
            case MouseEvent.BUTTON3 -> b = Button.RIGHT;
            default                 -> b = Button.OTHER;
        }
        pending = new Click(b, e.getX(), e.getY());
    }

    /**
     * Returns and clears the pending click, or {@code null} if none.
     *
     * @return the consumed click, or {@code null}
     */
    public synchronized Click consume() {
        final Click c = pending;
        pending = null;
        return c;
    }
}
