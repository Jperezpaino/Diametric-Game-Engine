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
     */
    public boolean canMove(final int fc, final int fr, final int tc, final int tr) {
        if (fc == tc && fr == tr) return true;
        final Tile from = standingTile(fc, fr);
        final Tile to   = standingTile(tc, tr);
        if (from == null || to == null) return false;
        if (!to.isMaterialWalkable())   return false;

        final int dc = tc - fc;
        final int dr = tr - fr;
        // Cardinal.
        if (Math.abs(dc) + Math.abs(dr) == 1) {
            final EdgeSide fromSide = sideFromDelta(dc, dr);
            return from.absoluteEdgeHeight(fromSide)
                == to.absoluteEdgeHeight(fromSide.opposite());
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

    /** Convenience using {@link Direction8}. */
    public boolean canMove(final int fc, final int fr, final Direction8 dir) {
        return canMove(fc, fr, fc + dir.dCol(), fr + dir.dRow());
    }

    private static EdgeSide sideFromDelta(final int dc, final int dr) {
        if (dc ==  1) return EdgeSide.EAST;
        if (dc == -1) return EdgeSide.WEST;
        if (dr ==  1) return EdgeSide.SOUTH;
        return EdgeSide.NORTH;
    }
}