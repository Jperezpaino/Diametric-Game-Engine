package es.noa.rad.entity;

import es.noa.rad.projection.WorldPoint;

/**
 * Base class for every entity that lives in the world (players, enemies,
 * interactive objects...).
 *
 * <p>Entities own their position and movement speed but never know how they
 * are rendered. Rendering belongs to the {@code render} package.</p>
 *
 * @since Phase 1
 */
public abstract class Entity {

    /** Position in world coordinates. */
    protected final WorldPoint position;

    /** Movement speed in tiles per second. */
    protected float speed;

    /**
     * Creates an entity at the given position.
     *
     * @param position initial position
     * @param speed    movement speed in tiles per second
     */
    protected Entity(final WorldPoint position, final float speed) {
        this.position = position;
        this.speed = speed;
    }

    public WorldPoint getPosition() { return position; }
    public float getSpeed() { return speed; }
    public void setSpeed(final float speed) { this.speed = speed; }

    /**
     * Updates the entity logic.
     *
     * @param deltaTime elapsed time in seconds since the previous update
     */
    public abstract void update(double deltaTime);
}
