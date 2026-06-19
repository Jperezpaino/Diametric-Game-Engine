package es.noa.rad.map;

import java.util.Map;
import java.util.Objects;

/**
 * Physical / gameplay identity of a {@link Tile}.
 *
 * <p>A {@code Material} is the <b>behaviour-only</b> half of a tile, decoupled
 * from visual appearance ({@link Skin}) and from geometry ({@link TileShape}).
 * It captures every property the engine cares about when an actor steps on,
 * collides with or otherwise interacts with the substance the tile is made
 * of.</p>
 *
 * <h3>Spec mapping</h3>
 * <ul>
 *   <li>{@link #solid} &mdash; whether the substance blocks line-of-movement
 *       (a stone block is solid; water is not).</li>
 *   <li>{@link #damage} &mdash; damage per second inflicted on actors
 *       standing on a tile of this material (lava &rArr; 5).</li>
 *   <li>{@link #speedModifier} &mdash; multiplier applied to the actor's
 *       walk speed (mud &rArr; 0.5, normal terrain &rArr; 1.0).</li>
 *   <li>{@link #properties} &mdash; open bag of extra flags / values for
 *       gameplay extensions ({@code "walkable"}, {@code "causesDrowning"}…)
 *       so the core record stays stable as new mechanics appear.</li>
 * </ul>
 *
 * @param id            Stable id used by registries and serialized files
 *                      (lowercase, snake_case, e.g. {@code "stone"}).
 * @param name          Human-readable display name (e.g. {@code "Stone"}).
 * @param solid         {@code true} when the substance physically blocks
 *                      movement through its volume.
 * @param damage        Damage per second applied while standing on the tile
 *                      ({@code 0} for harmless materials).
 * @param speedModifier Multiplier applied to the actor's movement speed
 *                      (1.0 = unchanged, &lt;1 = slower).
 * @param properties    Open map of additional behaviour flags / values.
 *                      Stored read-only; never {@code null}.
 *
 * @since Phase 8b
 */
public record Material(String id,
                       String name,
                       boolean solid,
                       float damage,
                       float speedModifier,
                       Map<String, Object> properties) {

    public Material {
        Objects.requireNonNull(id,   "id");
        Objects.requireNonNull(name, "name");
        if (id.isBlank()) throw new IllegalArgumentException("Material id must not be blank");
        properties = (properties == null) ? Map.of() : Map.copyOf(properties);
    }

    /** Returns the boolean property under {@code key}, or {@code defaultValue} when absent or wrong type. */
    public boolean boolProperty(final String key, final boolean defaultValue) {
        final Object raw = properties.get(key);
        return (raw instanceof Boolean b) ? b.booleanValue() : defaultValue;
    }

    /** Returns the numeric property under {@code key} as float, or {@code defaultValue} when absent or wrong type. */
    public float floatProperty(final String key, final float defaultValue) {
        final Object raw = properties.get(key);
        return (raw instanceof Number n) ? n.floatValue() : defaultValue;
    }

    /** Returns the string property under {@code key}, or {@code defaultValue} when absent or wrong type. */
    public String stringProperty(final String key, final String defaultValue) {
        final Object raw = properties.get(key);
        return (raw instanceof String s) ? s : defaultValue;
    }
}
