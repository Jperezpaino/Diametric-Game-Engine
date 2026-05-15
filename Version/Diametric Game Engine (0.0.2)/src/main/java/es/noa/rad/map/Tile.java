package es.noa.rad.map;

/**
 * Single cell of a {@link TileLayer}.
 *
 * <p>In Phase 1 a tile only carries its {@link TileType}. Additional metadata
 * (height, events, etc.) will be added in later phases as separate layers.</p>
 *
 * @since Phase 1
 */
public final class Tile {

    private final TileType type;

    /**
     * Creates a tile of the given type.
     *
     * @param type tile type, never {@code null}
     */
    public Tile(final TileType type) {
        this.type = type;
    }

    public TileType getType() { return type; }

    /** @return whether entities can walk over this tile. */
    public boolean isWalkable() {
        return type.isWalkable();
    }
}
