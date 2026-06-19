package es.noa.rad.map;

import java.awt.Color;

/**
 * Catalogue of tile <b>materials</b> (logical / gameplay layer). A material
 * defines what the surface <em>is</em>:
 * <ul>
 *   <li>its provisional render colour (until sprites exist),</li>
 *   <li>whether actors can step over it (walkable flag),</li>
 *   <li>its movement-speed factor (1.0 = normal, &lt;1 = slow),</li>
 *   <li>damage per second when standing on it,</li>
 *   <li>whether it causes drowning (liquid).</li>
 * </ul>
 *
 * <p>A material does <b>not</b> define the geometry of the tile. Geometry
 * lives in {@link TileShape}. A {@link Tile} is the combination of a
 * {@link TileMaterial}, a {@link TileShape} and an integer elevation.</p>
 *
 * @since Phase 3 (split from the legacy {@code TileType})
 */
public enum TileMaterial {

    GRASS (new Color( 80, 170,  80), true,  1.00f, 0f, false),
    STONE (new Color(150, 150, 150), true,  1.00f, 0f, false),
    WOOD  (new Color(160, 110,  60), true,  1.00f, 0f, false),
    SAND  (new Color(220, 200, 130), true,  0.85f, 0f, false),
    MUD   (new Color(110,  80,  40), true,  0.50f, 0f, false),
    SNOW  (new Color(235, 240, 245), true,  0.75f, 0f, false),
    WATER (new Color( 60, 110, 200), false, 0.40f, 0f, true ),
    LAVA  (new Color(220,  80,  30), true,  0.30f, 5f, false);

    private final Color   color;
    private final boolean walkable;
    private final float   speedFactor;
    private final float   damagePerSecond;
    private final boolean causesDrowning;

    TileMaterial(final Color color, final boolean walkable,
                 final float speedFactor, final float damagePerSecond,
                 final boolean causesDrowning) {
        this.color = color;
        this.walkable = walkable;
        this.speedFactor = speedFactor;
        this.damagePerSecond = damagePerSecond;
        this.causesDrowning = causesDrowning;
    }

    public Color   getColor()           { return color; }
    public boolean isWalkable()         { return walkable; }
    public float   getSpeedFactor()     { return speedFactor; }
    public float   getDamagePerSecond() { return damagePerSecond; }
    public boolean causesDrowning()     { return causesDrowning; }
}
