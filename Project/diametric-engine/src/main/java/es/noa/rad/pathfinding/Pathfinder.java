package es.noa.rad.pathfinding;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import es.noa.rad.map.Direction8;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileLayer;
import es.noa.rad.map.rules.MovementValidator;

/**
 * A* pathfinder over the top-floor of a {@link TileLayer}.
 *
 * <p>Edge legality is delegated to {@link MovementValidator}, which already
 * handles material walkability, edge-height matching and corner-cutting.
 * Costs:</p>
 * <ul>
 *   <li>Step cost = {@code 1} (cardinal) or {@code √2} (diagonal),
 *       divided by the destination material's {@code speedFactor} so slow
 *       terrain (mud, sand, snow) is correctly avoided when possible.</li>
 *   <li>Heuristic = <b>octile distance</b>, admissible for 8-direction grids.</li>
 * </ul>
 *
 * <p>The pathfinder is stateless apart from the {@link TileLayer} reference,
 * so a single instance can be reused across queries.</p>
 *
 * @since Phase 4c
 */
public final class Pathfinder {

    private static final double SQRT2 = Math.sqrt(2.0);

    private final TileLayer        layer;
    private final MovementValidator validator;

    public Pathfinder(final TileLayer layer) {
        this.layer     = layer;
        this.validator = new MovementValidator(layer);
    }

    /**
     * Computes the shortest walkable path from {@code (fc,fr)} to
     * {@code (tc,tr)}.
     *
     * @return the path including both endpoints, or an empty list if the
     *         destination is unreachable / out of bounds / not walkable.
     */
    public List<PathNode> findPath(final int fc, final int fr,
                                   final int tc, final int tr) {
        if (!layer.inBounds(fc, fr) || !layer.inBounds(tc, tr)) {
            return Collections.emptyList();
        }
        final Tile startTile = layer.getTopTile(fc, fr);
        final Tile goalTile  = layer.getTopTile(tc, tr);
        if (startTile == null || goalTile == null) return Collections.emptyList();
        if (!goalTile.isMaterialWalkable())        return Collections.emptyList();

        if (fc == tc && fr == tr) {
            return List.of(new PathNode(fc, fr));
        }

        final PathNode start = new PathNode(fc, fr);
        final PathNode goal  = new PathNode(tc, tr);

        final Map<PathNode, PathNode> cameFrom = new HashMap<>();
        final Map<PathNode, Double>   gScore   = new HashMap<>();
        gScore.put(start, 0.0);

        // Priority queue ordered by f = g + h.
        final PriorityQueue<Frontier> open = new PriorityQueue<>();
        open.add(new Frontier(start, heuristic(start, goal)));

        while (!open.isEmpty()) {
            final Frontier curr = open.poll();
            final PathNode  cn  = curr.node;
            if (cn.equals(goal)) {
                return reconstruct(cameFrom, cn);
            }
            // Lazy-deletion: skip stale entries.
            final double currG = gScore.getOrDefault(cn, Double.POSITIVE_INFINITY);
            if (curr.f > currG + heuristic(cn, goal) + 1e-9) continue;

            for (final Direction8 dir : Direction8.values()) {
                final int nc = cn.col() + dir.dCol();
                final int nr = cn.row() + dir.dRow();
                if (!validator.canMove(cn.col(), cn.row(), nc, nr)) continue;

                final Tile neighbourTile = layer.getTopTile(nc, nr);
                final float speed = neighbourTile.material().getSpeedFactor();
                if (speed <= 0f) continue; // defensive: unreachable terrain

                final double stepCost = (dir.isDiagonal() ? SQRT2 : 1.0) / speed;
                final double tentativeG = currG + stepCost;

                final PathNode neighbour = new PathNode(nc, nr);
                if (tentativeG < gScore.getOrDefault(neighbour, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(neighbour, cn);
                    gScore.put(neighbour, tentativeG);
                    open.add(new Frontier(neighbour, tentativeG + heuristic(neighbour, goal)));
                }
            }
        }
        return Collections.emptyList();
    }

    /** Octile distance — admissible for 8-direction movement on a unit grid. */
    private static double heuristic(final PathNode a, final PathNode b) {
        final int dx = Math.abs(a.col() - b.col());
        final int dy = Math.abs(a.row() - b.row());
        return (dx + dy) + (SQRT2 - 2.0) * Math.min(dx, dy);
    }

    private static List<PathNode> reconstruct(final Map<PathNode, PathNode> cameFrom,
                                              final PathNode goal) {
        final Deque<PathNode> stack = new ArrayDeque<>();
        PathNode cursor = goal;
        while (cursor != null) {
            stack.push(cursor);
            cursor = cameFrom.get(cursor);
        }
        return new ArrayList<>(stack);
    }

    /** Internal frontier entry; {@code Comparable} on {@code f}. */
    private static final class Frontier implements Comparable<Frontier> {
        final PathNode node;
        final double   f;

        Frontier(final PathNode node, final double f) {
            this.node = node;
            this.f    = f;
        }

        @Override public int compareTo(final Frontier o) {
            return Double.compare(this.f, o.f);
        }
    }
}
