package es.noa.rad.entity;

import es.noa.rad.projection.WorldPoint;

/**
 * Player entity controlled by the user.
 *
 * <p>In Phase 1 the player has a fixed position; movement and input handling
 * will be added in Phase 2.</p>
 *
 * @since Phase 1
 */
public final class Player extends Entity {

    /** Default speed in tiles per second. */
    public static final float DEFAULT_SPEED = 3f;

    /** Creates a player at column 3, row 3, z 0. */
    public Player() {
        super(new WorldPoint(3f, 3f, 0f), DEFAULT_SPEED);
    }

    @Override
    public void update(final double deltaTime) {
        // Phase 1: no movement yet.
    }
}
