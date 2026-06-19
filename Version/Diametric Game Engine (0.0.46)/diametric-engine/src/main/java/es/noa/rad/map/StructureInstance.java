package es.noa.rad.map;

import java.util.Objects;

/**
 * Absolute placement of a {@link Structure} inside a map.
 *
 * <p>The map adds the structure's tiles by translating each contained
 * {@link TileInstance} by {@code (x, y, z)} so the structure's origin
 * lands at this position. A single {@code Structure} may be instanced
 * any number of times across one or more maps.</p>
 *
 * @param structureId Stable id of the {@link Structure} to instantiate.
 * @param x           Absolute column where the structure's origin sits.
 * @param y           Absolute row    where the structure's origin sits.
 * @param z           Absolute elevation where the structure's origin sits.
 *
 * @since Phase 9a
 */
public record StructureInstance(String structureId, int x, int y, int z) {

    public StructureInstance {
        Objects.requireNonNull(structureId, "structureId");
        if (structureId.isBlank()) {
            throw new IllegalArgumentException("StructureInstance structureId must not be blank");
        }
    }
}
