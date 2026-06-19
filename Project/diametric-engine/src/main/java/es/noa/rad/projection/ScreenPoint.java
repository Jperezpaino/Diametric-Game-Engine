package es.noa.rad.projection;

/**
 * Immutable point in screen space, expressed in pixels.
 *
 * @since Phase 1
 */
public final class ScreenPoint {

    private final int x;
    private final int y;

    /**
     * Creates a screen point.
     *
     * @param x horizontal pixel coordinate
     * @param y vertical pixel coordinate
     */
    public ScreenPoint(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public String toString() {
        return "ScreenPoint(x=" + x + ", y=" + y + ")";
    }
}
