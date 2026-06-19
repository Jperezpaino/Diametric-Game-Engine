package es.noa.rad.pathfinding;

import java.util.Collections;
import java.util.List;

import es.noa.rad.entity.Player;
import es.noa.rad.map.Direction8;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.TileShape;
import es.noa.rad.map.rules.TerrainEffects;
import es.noa.rad.projection.WorldPoint;

/**
 * Moves a {@link Player} along a {@link PathNode} list at a speed modulated
 * by the material of the tile under foot. Once the final node is reached the
 * follower deactivates. Manual input can interrupt the follower at any time
 * via {@link #stop()}.
 *
 * @since Phase 4d
 */
public final class PathFollower {

    /** Distance (in tile units) at which a node counts as reached. */
    private static final float ARRIVAL_THRESHOLD = 0.08f;

    private List<PathNode> path = Collections.emptyList();
    private int index;

    public boolean isActive()      { return index < path.size(); }
    public List<PathNode> remaining() {
        return index >= path.size() ? Collections.emptyList()
                                    : path.subList(index, path.size());
    }

    /** Begins following {@code newPath}. Empty/null paths stop the follower. */
    public void setPath(final List<PathNode> newPath) {
        if (newPath == null || newPath.isEmpty()) {
            stop();
            return;
        }
        this.path  = newPath;
        this.index = 0;
    }

    /** Cancels the current follow. */
    public void stop() {
        this.path  = Collections.emptyList();
        this.index = 0;
    }

    /**
     * Advances the player toward the current target node.
     *
     * @param deltaTime elapsed seconds since the last update
     * @param player    entity to move
     * @param map       loaded map (for material-based speed and elevation)
     */
    public void update(final double deltaTime, final Player player, final TileMap map) {
        if (!isActive()) return;

        final WorldPoint pos = player.getPosition();
        final PathNode target = path.get(index);
        final float dx = target.col() - pos.getCol();
        final float dy = target.row() - pos.getRow();
        final float dist = (float) Math.hypot(dx, dy);

        if (dist <= ARRIVAL_THRESHOLD) {
            pos.setCol(target.col());
            pos.setRow(target.row());
            pos.setZ(surfaceElevation(map, target.col(), target.row()));
            index++;
            return;
        }

        player.setFacing(Direction8.fromWorldDelta(dx, dy, player.getFacing()));

        final int cc = Math.round(pos.getCol());
        final int cr = Math.round(pos.getRow());
        final Tile under = map.getTopTile(cc, cr);
        final boolean diagonal = dx != 0f && dy != 0f;
        final float speedFactor = TerrainEffects.combinedSpeedFactor(under, diagonal, false);
        final float step = (float) (player.getSpeed() * speedFactor * deltaTime);
        final float k = Math.min(1f, step / dist);

        pos.setCol(pos.getCol() + dx * k);
        pos.setRow(pos.getRow() + dy * k);
        pos.setZ(surfaceElevation(map, pos.getCol(), pos.getRow()));
    }

    /**
     * Bilinear interpolation of the four absolute corner heights of the top
     * tile under (col, row). Matches the formula used by {@code Player} so
     * the follower walks up and down ramps smoothly.
     */
    private static float surfaceElevation(final TileMap map, final float col, final float row) {
        final int c = Math.round(col);
        final int r = Math.round(row);
        final Tile tile = map.getTopTile(c, r);
        if (tile == null) return 0f;
        final float u = (col - c) + 0.5f;
        final float v = (row - r) + 0.5f;
        final float nw = tile.absoluteCornerHeight(TileShape.Corner.NW);
        final float ne = tile.absoluteCornerHeight(TileShape.Corner.NE);
        final float se = tile.absoluteCornerHeight(TileShape.Corner.SE);
        final float sw = tile.absoluteCornerHeight(TileShape.Corner.SW);
        return nw * (1 - u) * (1 - v)
             + ne *      u  * (1 - v)
             + se *      u  *      v
             + sw * (1 - u) *      v;
    }
}
