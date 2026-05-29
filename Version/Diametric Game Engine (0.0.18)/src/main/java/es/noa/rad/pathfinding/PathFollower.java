package es.noa.rad.pathfinding;

import java.util.Collections;
import java.util.List;

import es.noa.rad.entity.Player;
import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMap;
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
            final Tile t = map.getTopTile(target.col(), target.row());
            if (t != null) pos.setZ(t.elevation());
            index++;
            return;
        }

        final int cc = Math.round(pos.getCol());
        final int cr = Math.round(pos.getRow());
        final Tile under = map.getTopTile(cc, cr);
        final boolean diagonal = dx != 0f && dy != 0f;
        final float speedFactor = TerrainEffects.combinedSpeedFactor(under, diagonal, false);
        final float step = (float) (player.getSpeed() * speedFactor * deltaTime);
        final float k = Math.min(1f, step / dist);

        pos.setCol(pos.getCol() + dx * k);
        pos.setRow(pos.getRow() + dy * k);

        final Tile here = map.getTopTile(Math.round(pos.getCol()), Math.round(pos.getRow()));
        if (here != null) pos.setZ(here.elevation());
    }
}
