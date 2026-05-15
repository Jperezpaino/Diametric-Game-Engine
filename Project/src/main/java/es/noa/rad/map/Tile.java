package es.noa.rad.map;

/**
 * Single cell of a {@link TileLayer}.
 *
 * <p>Carries a {@link TileType} (visual + walkability) and an integer
 * {@code elevation} measured in tile-height units along the world Z axis.
 * Elevation is independent from {@link TileType#getElevationDelta()}: the
 * former is the absolute height of the tile, the latter is the change applied
 * to an entity that steps on top of it.</p>
 *
 * @since Phase 1 (elevation added Phase 3)
 */
public final class Tile {

    private final TileType type;
    private final int elevation;

    /**
     * Creates a tile of the given type at elevation {@code 0}.
     *
     * @param type tile type, never {@code null}
     */
    public Tile(final TileType type) {
        this(type, 0);
    }

    /**
     * Creates a tile of the given type at the given elevation.
     *
     * @param type      tile type, never {@code null}
     * @param elevation height in tile-height units (0 = ground)
     */
    public Tile(final TileType type, final int elevation) {
        this.type = type;
        this.elevation = elevation;
    }

    public TileType getType()      { return type; }
    public int      getElevation() { return elevation; }

    /** @return whether entities can walk over this tile. */
    public boolean isWalkable() {
        return type.isWalkable();
    }
}
