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
}
