package es.noa.rad.input;

import java.awt.event.KeyEvent;

/**
 * Centralised, thread-safe snapshot of the keyboard state.
 *
 * <p>The array is indexed by {@link KeyEvent} VK constants. A value of
 * {@code true} means the key is currently held down. All mutations come from
 * the Event Dispatch Thread via {@link InputHandler}; reads happen from the
 * game-loop thread, so the array is declared {@code volatile}-element-safe
 * through the {@code synchronized} helpers.</p>
 *
 * @since Phase 2
 */
public final class InputState {

    /** Total number of virtual-key codes tracked (standard AWT range). */
    private static final int KEY_COUNT = 256;

    private final boolean[] keys = new boolean[KEY_COUNT];

    // -------------------------------------------------------------------------
    // Mutation (called from EDT via InputHandler)
    // -------------------------------------------------------------------------

    /**
     * Marks {@code code} as pressed.
     *
     * @param code {@link KeyEvent} VK constant
     */
    public synchronized void keyPressed(final int code) {
        if (code >= 0 && code < KEY_COUNT) {
            keys[code] = true;
        }
    }

    /**
     * Marks {@code code} as released.
     *
     * @param code {@link KeyEvent} VK constant
     */
    public synchronized void keyReleased(final int code) {
        if (code >= 0 && code < KEY_COUNT) {
            keys[code] = false;
        }
    }

    // -------------------------------------------------------------------------
    // Query (called from game-loop thread)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} while the key identified by {@code code} is held.
     *
     * @param code {@link KeyEvent} VK constant
     * @return whether the key is currently down
     */
    public synchronized boolean isDown(final int code) {
        return code >= 0 && code < KEY_COUNT && keys[code];
    }
}
