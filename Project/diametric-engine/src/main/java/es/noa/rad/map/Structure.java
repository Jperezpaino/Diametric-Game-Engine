package es.noa.rad.map;

import java.util.List;
import java.util.Objects;

/**
 * Reusable group of {@link TileInstance}s organised relative to a fixed
 * origin at {@code (0, 0, 0)}.
 *
 * <p>Structures exist to factor out repeating spatial patterns &mdash;
 * columns, houses, stairs, trees, walls, bridges &mdash; so they can be
 * defined once and instanced many times across one or more maps via
 * {@link StructureInstance}. The internal coordinates of the contained
 * tiles are <b>relative</b> to the structure's origin; the map-level
 * placement supplies the absolute offset.</p>
 *
 * <p>Spec mapping (clause 10):</p>
 * <ul>
 *   <li>An origin tile, if present, is the entry at relative {@code (0, 0, 0)};
 *       its lower corner is the structural anchor used for placement
 *       arithmetic.</li>
 *   <li>A structure may contain any number of tiles.</li>
 *   <li>The same {@link TileDefinition} may be referenced by multiple
 *       tiles within the structure, by other structures, or directly by
 *       the map.</li>
 * </ul>
 *
 * @param id    Stable id used by registries and serialised files
 *              (lowercase, snake_case, e.g. {@code "small_house"}).
 * @param name  Human-readable display name (e.g. {@code "Small House"}).
 * @param tiles Read-only list of {@link TileInstance}s relative to the
 *              structure's origin. Never {@code null} (an empty structure
 *              is a valid edge case while authoring).
 *
 * @since Phase 9a
 */
public record Structure(String id, String name, List<TileInstance> tiles) {

    public Structure {
        Objects.requireNonNull(id,   "id");
        Objects.requireNonNull(name, "name");
        if (id.isBlank()) throw new IllegalArgumentException("Structure id must not be blank");
        tiles = (tiles == null) ? List.of() : List.copyOf(tiles);
    }
}
