package es.noa.rad.entity;

import es.noa.rad.map.Direction8;
import es.noa.rad.projection.WorldPoint;

/**
 * Base class for every entity that lives in the world (players, enemies,
 * interactive objects...).
 *
 * <p>Entities own their position, movement speed, health and facing direction
 * but never know how they are rendered. Rendering belongs to the
 * {@code render} package.</p>
 *
 * @since Phase 1 (HP + facing added Phase 3 debt-close)
 */
public abstract class Entity {

    /** Default maximum hit-points assigned to new entities. */
    public static final float DEFAULT_MAX_HP = 100f;

    /** Position in world coordinates. */
    protected final WorldPoint position;

    /** Movement speed in tiles per second. */
    protected float speed;

    /** Current hit-points. Reaches 0 when the entity dies. */
    protected float hp;

    /** Maximum hit-points. */
    protected float maxHp;

    /** Direction this entity is currently facing. Default: South. */
    protected Direction8 facing = Direction8.S;

    /**
     * Creates an entity at the given position with full HP.
     *
     * @param position initial position
     * @param speed    movement speed in tiles per second
     */
    protected Entity(final WorldPoint position, final float speed) {
        this.position = position;
        this.speed    = speed;
        this.maxHp    = DEFAULT_MAX_HP;
        this.hp       = maxHp;
    }

    // ---- position / speed ---------------------------------------------------

    public WorldPoint getPosition() { return position; }
    public float getSpeed()         { return speed; }
    public void setSpeed(final float speed) { this.speed = speed; }

    // ---- health -------------------------------------------------------------

    /** @return current HP (0 = dead). */
    public float getHp()    { return hp; }

    /** @return maximum HP. */
    public float getMaxHp() { return maxHp; }

    /** @return {@code true} if {@link #hp} is above zero. */
    public boolean isAlive() { return hp > 0f; }

    /**
     * Applies damage to this entity. HP is clamped to [0, maxHp].
     *
     * @param amount damage to apply (positive value)
     */
    public void damage(final float amount) {
        hp = Math.max(0f, hp - amount);
    }

    /**
     * Heals this entity. HP is clamped to maxHp.
     *
     * @param amount HP to restore (positive value)
     */
    public void heal(final float amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    // ---- facing -------------------------------------------------------------

    /** @return the direction this entity is currently facing. */
    public Direction8 getFacing() { return facing; }

    /** Updates the facing direction. */
    public void setFacing(final Direction8 facing) {
        if (facing != null) this.facing = facing;
    }

    // ---- subclass contract --------------------------------------------------

    /**
     * Updates the entity logic.
     *
     * @param deltaTime elapsed time in seconds since the previous update
     */
    public abstract void update(double deltaTime);
}
