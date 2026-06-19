package es.noa.rad.map;

import java.util.Objects;

/**
 * Catalogue entry that composes a {@link Skin}, a {@link Material} and a
 * {@link TileShape} into a single, named, reusable tile recipe.
 *
 * <p>This is what the Map System spec calls a <em>Tile</em>: the abstract
 * "what" of a tile (its visual identity, its physical behaviour, its
 * geometry) decoupled from "where" it sits in the world. Many runtime
 * {@link Tile} voxels can share the same {@code TileDefinition}.</p>
 *
 * <p>Phase 8d introduces this record and {@link TileDefinitionRegistry}
 * alongside the existing {@code (TileMaterial, TileShape, elevation)} runtime
 * tuple. Phase 8d-bis migrates the runtime to reference definition ids
 * instead of the legacy enum pair.</p>
 *
 * @param id         Stable id used by registries and serialized files
 *                   (lowercase, snake_case, e.g. {@code "stone_ramp_ne"}).
 * @param name       Human-readable display name (e.g. {@code "Stone Ramp NE"}).
 * @param skinId     {@link Skin} id resolved through {@code SkinRegistry}.
 * @param materialId {@link Material} id resolved through
 *                   {@link MaterialRegistry}.
 * @param shape      Geometric {@link TileShape} (kept as enum: shapes are
 *                   intrinsic to the rendering / pathfinding code, not
 *                   data-driven).
 *
 * @since Phase 8d
 */
public record TileDefinition(String id,
                             String name,
                             String skinId,
                             String materialId,
                             TileShape shape) {

    public TileDefinition {
        Objects.requireNonNull(id,         "id");
        Objects.requireNonNull(name,       "name");
        Objects.requireNonNull(skinId,     "skinId");
        Objects.requireNonNull(materialId, "materialId");
        Objects.requireNonNull(shape,      "shape");
        if (id.isBlank())         throw new IllegalArgumentException("TileDefinition id must not be blank");
        if (skinId.isBlank())     throw new IllegalArgumentException("TileDefinition skinId must not be blank");
        if (materialId.isBlank()) throw new IllegalArgumentException("TileDefinition materialId must not be blank");
    }
}
