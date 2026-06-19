package es.noa.rad.map.rules;

import es.noa.rad.map.Tile;
import es.noa.rad.map.TileMaterial;

/**
 * Stateless helper that returns the gameplay effects of standing on a tile:
 * speed multiplier, damage per second and drowning. Pure functions over
 * {@link TileMaterial}.
 *
 * @since Phase 3
 */
public final class TerrainEffects {

    /** Extra speed penalty when moving diagonally (player feels heavier). */
    public static final float DIAGONAL_SPEED_FACTOR = 0.85f;

    /** Speed penalty applied per integer level of climb (Z) per second. */
    public static final float CLIMB_SPEED_FACTOR = 0.75f;

    private TerrainEffects() {}

    public static float speedMultiplier(final Tile tile) {
        return tile == null ? 1f : tile.material().getSpeedFactor();
    }

    public static float damagePerSecond(final Tile tile) {
        return tile == null ? 0f : tile.material().getDamagePerSecond();
    }

    public static boolean isDrowning(final Tile tile) {
        return tile != null && tile.material().causesDrowning();
    }

    /**
     * Combines material speed, diagonal penalty and climb penalty.
     *
     * @param tile         tile being stepped on
     * @param diagonal     whether the move is diagonal
     * @param climbingUp   whether the actor is gaining elevation this tick
     */
    public static float combinedSpeedFactor(final Tile tile,
                                            final boolean diagonal,
                                            final boolean climbingUp) {
        float f = speedMultiplier(tile);
        if (diagonal)   f *= DIAGONAL_SPEED_FACTOR;
        if (climbingUp) f *= CLIMB_SPEED_FACTOR;
        return f;
    }
}