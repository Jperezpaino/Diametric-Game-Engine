package es.noa.rad.map;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalogue of {@link Structure}s, keyed by their stable id.
 *
 * <p>Mirrors the shape of {@link TileDefinitionRegistry} and
 * {@link MaterialRegistry}: insertion-ordered lookups, simple
 * {@code register} / {@code get} accessors, and a
 * {@link #loadDefaults() loadDefaults()} factory that the rest of the
 * engine can rely on without each call site having to thread a registry
 * through.</p>
 *
 * <p>The default registry is intentionally <b>empty</b>: structures are
 * pure user content (Phase 9b will load them from {@code StructureSet}
 * JSON files; Phase 12 will let the Structure Builder author them).
 * Callers that need a populated registry must register their own
 * structures.</p>
 *
 * @since Phase 9a
 */
public final class StructureRegistry {

    private final Map<String, Structure> byId = new LinkedHashMap<>();

    /** Adds or replaces a structure in the registry. */
    public void register(final Structure structure) {
        byId.put(structure.id(), structure);
    }

    /** Returns the structure registered under {@code id}, or {@code null} when unknown. */
    public Structure get(final String id) {
        return id == null ? null : byId.get(id);
    }

    /** Read-only view of every registered structure (insertion order preserved). */
    public Collection<Structure> structures() {
        return byId.values();
    }

    /** Builds an empty default registry; structures are loaded from resource files. */
    public static StructureRegistry loadDefaults() {
        return new StructureRegistry();
    }
}
