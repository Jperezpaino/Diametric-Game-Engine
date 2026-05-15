package es.noa.rad.map;

import java.awt.Color;

/**
 * Catalogue of tile types available in Phase 1.
 *
 * <p>Each value carries its debug colour (used while the engine renders with
 * primitive shapes) and a {@code walkable} flag consumed by the future
 * collision system.</p>
 *
 * @since Phase 1
 */
public enum TileType {

    /** Walkable grass. */
    GRASS(new Color(80, 170, 80), true),

    /** Non-walkable water. */
    WATER(new Color(60, 110, 200), false),

    /** Solid wall, blocks movement. */
    WALL(new Color(120, 120, 120), false);

    private final Color color;
    private final boolean walkable;

    TileType(final Color color, final boolean walkable) {
        this.color = color;
        this.walkable = walkable;
    }

    public Color getColor() { return color; }
    public boolean isWalkable() { return walkable; }
}
