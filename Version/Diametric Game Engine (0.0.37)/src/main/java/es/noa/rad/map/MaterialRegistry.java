package es.noa.rad.map;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalogue of {@link Material} definitions, keyed by their stable id.
 *
 * <p>Acts as the single source of truth for material metadata. The runtime
 * may carry materials around through references (id) instead of duplicating
 * their fields on every tile.</p>
 *
 * <h3>Default material set (Phase 8b)</h3>
 * <p>{@link #loadDefaults()} seeds one {@link Material} per {@link TileMaterial}
 * enum value, mirroring the legacy fields:</p>
 * <ul>
 *   <li>{@code id} = enum name lowercase (e.g. {@code "grass"}).</li>
 *   <li>{@code name} = human-friendly capitalization.</li>
 *   <li>{@code solid} = whether the substance blocks movement; liquids
 *       ({@code WATER}, {@code LAVA}) are non-solid.</li>
 *   <li>{@code damage} = legacy damage-per-second.</li>
 *   <li>{@code speedModifier} = legacy speed factor.</li>
 *   <li>{@code properties} = {@code "walkable"} (bool) and
 *       {@code "causesDrowning"} (bool) preserved for the engine's existing
 *       gameplay rules.</li>
 * </ul>
 *
 * @since Phase 8b
 */
public final class MaterialRegistry {

    private final Map<String, Material> byId = new LinkedHashMap<>();

    /** Adds or replaces a material in the registry. */
    public void register(final Material material) {
        byId.put(material.id(), material);
    }

    /** Returns the material registered under {@code id}, or {@code null} when unknown. */
    public Material get(final String id) {
        return id == null ? null : byId.get(id);
    }

    /** Read-only view of every registered material (insertion order preserved). */
    public Collection<Material> materials() {
        return byId.values();
    }

    /** Builds the default material set from the legacy {@link TileMaterial} enum. */
    public static MaterialRegistry loadDefaults() {
        final MaterialRegistry registry = new MaterialRegistry();
        for (final TileMaterial legacy : TileMaterial.values()) {
            registry.register(legacy.toMaterial());
        }
        return registry;
    }
}
