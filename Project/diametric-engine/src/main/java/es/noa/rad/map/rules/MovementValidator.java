package es.noa.rad.map.rules;

import es.noa.rad.map.Direction8;
import es.noa.rad.map.EdgeSide;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;

/**
 * Stateless service that decides whether an actor standing on a given cell
 * may move into a neighbouring cell.
 *
 * <p>Rules implemented:</p>
 * <ul>
 *   <li><b>Bounds &amp; material</b>: destination must exist and its material
 *       must be walkable.</li>
 *   <li><b>Cardinal step</b>: the shared edge between source and destination
 *       must have exactly the same absolute height on both sides.</li>
 *   <li><b>Diagonal step</b>: the shared corner must match in height AND
 *       both adjacent cardinals must individually be walkable
 *       (no &quot;corner cutting&quot; through walls).</li>
 *   <li><b>Walk-on-block</b>: a {@link es.noa.rad.map.TileShape#BLOCK} with
 *       no tile above is treated as walkable on its top face.</li>
 * </ul>
 *
 * @since Phase 3
 */
public final class MovementValidator {

    private final TileLayer layer;

    public MovementValidator(final TileLayer layer) {
        this.layer = layer;
    }

    /**
     * Returns the tile an actor at integer cell (col,row) is standing ON,
     * i.e. the topmost tile in the column.
     */
    public Tile standingTile(final int col, final int row) {
        return layer.getTopTile(col, row);
    }

    /**
     * Returns whether moving from (fc,fr) to (tc,tr) is allowed. The two
     * cells must be neighbours (cardinal or diagonal); otherwise this
     * returns {@code false}.
     *
     * <h3>Cardinal edge check (bit-to-corner mapping)</h3>
     * <p>Each tile bit directly equals a corner height (N→NW, E→NE, S→SE,
     * W→SW). A shared edge between two adjacent tiles has TWO corners; both
     * must match for the crossing to be valid (partial walls are blocked).</p>
     *
     * <pre>
     *   Move EAST : FROM(NE=E, SE=S)  ↔  TO(NW=N, SW=W)
     *               → FROM.E==TO.N  AND  FROM.S==TO.W
     *   Move WEST : FROM(NW=N, SW=W)  ↔  TO(NE=E, SE=S)
     *               → FROM.N==TO.E  AND  FROM.W==TO.S
     *   Move SOUTH: FROM(SE=S, SW=W)  ↔  TO(NE=E, NW=N)
     *               → FROM.S==TO.E  AND  FROM.W==TO.N
     *   Move NORTH: FROM(NW=N, NE=E)  ↔  TO(SW=W, SE=S)
     *               → FROM.N==TO.W  AND  FROM.E==TO.S
     * </pre>
     */
    public boolean canMove(final int fc, final int fr, final int tc, final int tr) {
        if (fc == tc && fr == tr) return true;
        final Tile from = standingTile(fc, fr);
        final Tile to   = standingTile(tc, tr);
        if (from == null || to == null) return false;
        if (!to.isMaterialWalkable())   return false;

        final int dc = tc - fc;
        final int dr = tr - fr;

        // Cardinal: check BOTH corners of the shared edge.
        if (Math.abs(dc) + Math.abs(dr) == 1) {
            return edgePassable(from, to, dc, dr);
        }
        // Diagonal: both intermediate cardinals must be walkable too.
        if (Math.abs(dc) == 1 && Math.abs(dr) == 1) {
            return canMove(fc, fr, fc + dc, fr)
                && canMove(fc, fr, fc, fr + dr)
                && canMove(fc + dc, fr, tc, tr)
                && canMove(fc, fr + dr, tc, tr);
        }
        return false;
    }

    /**
     * Checks that the two corners on the shared edge between {@code from} and
     * {@code to} have equal absolute heights. {@code dc}/{@code dr} is the
     * unit movement delta (exactly one of the four cardinal directions).
     */
    private static boolean edgePassable(final Tile from, final Tile to,
                                         final int dc, final int dr) {
        // Bit accessors for readability (elevation already baked in via absolute*).
        final int fN = from.absoluteEdgeHeight(EdgeSide.NORTH);
        final int fE = from.absoluteEdgeHeight(EdgeSide.EAST);
        final int fS = from.absoluteEdgeHeight(EdgeSide.SOUTH);
        final int fW = from.absoluteEdgeHeight(EdgeSide.WEST);
        final int tN = to.absoluteEdgeHeight(EdgeSide.NORTH);
        final int tE = to.absoluteEdgeHeight(EdgeSide.EAST);
        final int tS = to.absoluteEdgeHeight(EdgeSide.SOUTH);
        final int tW = to.absoluteEdgeHeight(EdgeSide.WEST);

        if (dc ==  1) return fE == tN && fS == tW;  // EAST  : FROM(NE,SE) ↔ TO(NW,SW)
        if (dc == -1) return fN == tE && fW == tS;  // WEST  : FROM(NW,SW) ↔ TO(NE,SE)
        if (dr ==  1) return fS == tE && fW == tN;  // SOUTH : FROM(SE,SW) ↔ TO(NE,NW)
                      return fN == tW && fE == tS;  // NORTH : FROM(NW,NE) ↔ TO(SW,SE)
    }

    /** Convenience using {@link Direction8}. */
    public boolean canMove(final int fc, final int fr, final Direction8 dir) {
        return canMove(fc, fr, fc + dir.dCol(), fr + dir.dRow());
    }
}