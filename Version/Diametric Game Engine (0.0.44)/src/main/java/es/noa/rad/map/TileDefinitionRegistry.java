package es.noa.rad.map;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalogue of {@link TileDefinition}s, keyed by their stable id.
 *
 * <p>Acts as the single source of truth for tile recipes
 * ({@code Skin} + {@code Material} + {@code TileShape}). Eventually each
 * runtime voxel will reference a definition by id; for now the registry
 * lives alongside the legacy {@code (TileMaterial, TileShape)} runtime
 * tuple (see {@code Tile}).</p>
 *
 * <h3>Default definition set (Phase 8d)</h3>
 * <p>{@link #loadDefaults()} seeds one definition per {@code (TileMaterial,
 * TileShape)} pair, mirroring the synthetic ids used by
 * {@code SkinRegistry} (Phase 8a) and {@link MaterialRegistry} (Phase 8b):</p>
 * <ul>
 *   <li>{@code id}         = {@code "<material>_<shape>"} (lowercase).</li>
 *   <li>{@code name}       = {@code "<Material display> <Shape display>"}
 *       (e.g. {@code "Stone Ramp NE"}).</li>
 *   <li>{@code skinId}     = same synthetic id (matches the default skin).</li>
 *   <li>{@code materialId} = legacy enum's {@link TileMaterial#materialId()}.</li>
 *   <li>{@code shape}      = the legacy {@link TileShape} enum value.</li>
 * </ul>
 *
 * @since Phase 8d
 */
public final class TileDefinitionRegistry {

    /**
     * Lazily-initialised default registry used by the legacy
     * {@code Tile(TileMaterial, TileShape, int)} convenience constructor.
     *
     * <p>Seeded once via {@link #loadDefaults()} so every runtime tile built
     * from the legacy enum pair resolves to a real {@link TileDefinition}
     * reference without each call site having to thread a registry through.</p>
     */
    public static final TileDefinitionRegistry DEFAULT = loadDefaults();

    private final Map<String, TileDefinition> byId = new LinkedHashMap<>();

    /** Adds or replaces a definition in the registry. */
    public void register(final TileDefinition definition) {
        byId.put(definition.id(), definition);
    }

    /** Returns the definition registered under {@code id}, or {@code null} when unknown. */
    public TileDefinition get(final String id) {
        return id == null ? null : byId.get(id);
    }

    /** Read-only view of every registered definition (insertion order preserved). */
    public Collection<TileDefinition> definitions() {
        return byId.values();
    }

    /** Synthetic definition id derived from a {@code (material, shape)} pair. */
    public static String defaultId(final TileMaterial material, final TileShape shape) {
        return material.materialId() + "_" + shape.shapeId();
    }

    /** Builds the default definition set (one entry per {@code (material, shape)} pair). */
    public static TileDefinitionRegistry loadDefaults() {
        final TileDefinitionRegistry registry = new TileDefinitionRegistry();
        for (final TileMaterial material : TileMaterial.values()) {
            for (final TileShape shape : TileShape.values()) {
                final String id   = defaultId(material, shape);
                final String name = capitalize(material.materialId()) + " " + shape.displayName();
                registry.register(new TileDefinition(
                        id, name, id, material.materialId(), shape));
            }
        }
        return registry;
    }

    private static String capitalize(final String raw) {
        if (raw.isEmpty()) return raw;
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
