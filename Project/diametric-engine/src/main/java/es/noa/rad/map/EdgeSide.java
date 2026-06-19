package es.noa.rad.map;

/**
 * Cardinal sides of a tile&#39;s top face. Used to address the four edge
 * heights stored in {@link TileShape} and to encode neighbour-direction
 * lookups.
 *
 * <p>Each value carries the column / row delta that points to the
 * neighbouring tile across that edge.</p>
 *
 * @since Phase 3 (corner-height redesign)
 */
public enum EdgeSide {

    NORTH( 0, -1),
    EAST ( 1,  0),
    SOUTH( 0,  1),
    WEST (-1,  0);

    private final int dCol;
    private final int dRow;

    EdgeSide(final int dCol, final int dRow) {
        this.dCol = dCol;
        this.dRow = dRow;
    }

    public int dCol() { return dCol; }
    public int dRow() { return dRow; }

    /** @return the side facing in the opposite direction. */
    public EdgeSide opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST:  return WEST;
            case WEST:  return EAST;
            default: throw new IllegalStateException("Unknown side: " + this);
        }
    }
}
