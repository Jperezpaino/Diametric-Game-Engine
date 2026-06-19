package es.noa.rad.map;

import java.util.Objects;

/**
 * Spatial placement of a {@link TileDefinition} inside a parent container.
 *
 * <p>Two containers consume {@code TileInstance}s:</p>
 * <ul>
 *   <li>A {@link Structure}, where {@code (x, y, z)} are <b>relative</b> to
 *       the structure's origin {@code (0, 0, 0)}.</li>
 *   <li>A map (Phase 9c), where {@code (x, y, z)} are <b>absolute</b> world
 *       coordinates.</li>
 * </ul>
 *
 * <p>The interpretation of the coordinates is owned by the container; the
 * record itself carries no coordinate-space tag because the same wire shape
 * is reused on disk for both cases.</p>
 *
 * @param tileId Stable id of the {@link TileDefinition} to be placed.
 * @param x      Column inside the parent container.
 * @param y      Row    inside the parent container.
 * @param z      Elevation slot inside the parent container.
 *
 * @since Phase 9a
 */
public record TileInstance(String tileId, int x, int y, int z) {

    public TileInstance {
        Objects.requireNonNull(tileId, "tileId");
        if (tileId.isBlank()) throw new IllegalArgumentException("TileInstance tileId must not be blank");
    }
}
