package es.noa.rad.map;

/**
 * The 8 grid-aligned directions an actor can move on the diametric world.
 * Each value carries the column / row delta in tile units.
 *
 * <p>Diagonals are made explicit so the navigation rules (corner-cutting
 * checks, double cardinal validation) can be expressed once and reused.</p>
 *
 * @since Phase 3 (corner-height redesign)
 */
public enum Direction8 {

    N ( 0, -1),
    NE( 1, -1),
    E ( 1,  0),
    SE( 1,  1),
    S ( 0,  1),
    SW(-1,  1),
    W (-1,  0),
    NW(-1, -1);

    private final int dCol;
    private final int dRow;

    Direction8(final int dCol, final int dRow) {
        this.dCol = dCol;
        this.dRow = dRow;
    }

    public int dCol() { return dCol; }
    public int dRow() { return dRow; }

    /** @return {@code true} for NE/SE/SW/NW. */
    public boolean isDiagonal() { return dCol != 0 && dRow != 0; }

    /** @return horizontal (E/W) cardinal component, or {@code null} for pure N/S. */
    public EdgeSide horizontalComponent() {
        if (dCol > 0) return EdgeSide.EAST;
        if (dCol < 0) return EdgeSide.WEST;
        return null;
    }

    /** @return vertical (N/S) cardinal component, or {@code null} for pure E/W. */
    public EdgeSide verticalComponent() {
        if (dRow > 0) return EdgeSide.SOUTH;
        if (dRow < 0) return EdgeSide.NORTH;
        return null;
    }

    /**
     * Derives a {@code Direction8} from a continuous world-space delta vector.
     * Uses the sign of each component so any magnitude works.
     *
     * @param dCol     column delta (positive = East)
     * @param dRow     row delta (positive = South)
     * @param fallback returned when the delta is zero (entity not moving)
     * @return closest {@code Direction8}, or {@code fallback} if {@code (dCol,dRow) == (0,0)}
     */
    public static Direction8 fromWorldDelta(final float dCol, final float dRow,
                                            final Direction8 fallback) {
        final int dc = (int) Math.signum(dCol);
        final int dr = (int) Math.signum(dRow);
        if (dc == 0 && dr == 0) return fallback;
        for (final Direction8 d : values()) {
            if (d.dCol == dc && d.dRow == dr) return d;
        }
        return fallback;
    }
}
