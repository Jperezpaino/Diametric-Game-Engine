package es.noa.rad.map;

import java.util.Objects;

/**
 * Visual identity of a {@link Tile}.
 *
 * <p>A {@code Skin} is the <b>appearance-only</b> half of a tile, decoupled
 * from physical behaviour ({@link TileMaterial}) and from geometry
 * ({@link TileShape}). It carries a stable identifier, a human-readable
 * label, and a classpath reference to the image asset that represents the
 * tile on screen.</p>
 *
 * <p>The same {@code Skin} can be reused by tiles built on different
 * materials or shapes &mdash; e.g. a "stone_block" skin painted on top of a
 * "wood" material to fake a stone-clad wooden box.</p>
 *
 * @param id       Stable id used by registries and serialized files
 *                 (lowercase, snake_case, e.g. {@code "grass_floor"}).
 * @param name     Human-readable display name (e.g. {@code "Grass Floor"}).
 * @param imageRef Classpath path to the PNG asset
 *                 (e.g. {@code "tiles/grass_floor.png"}).
 *
 * @since Phase 8a
 */
public record Skin(String id, String name, String imageRef) {

    public Skin {
        Objects.requireNonNull(id,       "id");
        Objects.requireNonNull(name,     "name");
        Objects.requireNonNull(imageRef, "imageRef");
        if (id.isBlank())       throw new IllegalArgumentException("Skin id must not be blank");
        if (imageRef.isBlank()) throw new IllegalArgumentException("Skin imageRef must not be blank");
    }
}
