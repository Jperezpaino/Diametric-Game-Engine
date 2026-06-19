package es.noa.rad.map;

import java.awt.Color;
import java.util.Map;

/**
 * Catalogue of tile <b>materials</b> (logical / gameplay layer). A material
 * defines what the surface <em>is</em>:
 * <ul>
 *   <li>its provisional render colour (until sprites exist),</li>
 *   <li>whether actors can step over it (walkable flag),</li>
 *   <li>whether the substance physically blocks movement (solid flag),</li>
 *   <li>its movement-speed factor (1.0 = normal, &lt;1 = slow),</li>
 *   <li>damage per second when standing on it,</li>
 *   <li>whether it causes drowning (liquid).</li>
 * </ul>
 *
 * <p>A material does <b>not</b> define the geometry of the tile. Geometry
 * lives in {@link TileShape}. A {@link Tile} is the combination of a
 * {@link TileMaterial}, a {@link TileShape} and an integer elevation.</p>
 *
 * <p>Phase 8b introduced the {@link Material} record + {@link MaterialRegistry}
 * as the canonical, registry-backed representation. This enum still acts as
 * the legacy carrier for tile data while the rest of the engine migrates;
 * {@link #toMaterial()} converts an enum value into its registry record.</p>
 *
 * @since Phase 3 (split from the legacy {@code TileType})
 */
public enum TileMaterial {

    GRASS (new Color( 80, 170,  80), true,  true,  1.00f, 0f, false),
    STONE (new Color(150, 150, 150), true,  true,  1.00f, 0f, false),
    WOOD  (new Color(160, 110,  60), true,  true,  1.00f, 0f, false),
    SAND  (new Color(220, 200, 130), true,  true,  0.85f, 0f, false),
    MUD   (new Color(110,  80,  40), true,  true,  0.50f, 0f, false),
    SNOW  (new Color(235, 240, 245), true,  true,  0.75f, 0f, false),
    WATER (new Color( 60, 110, 200), false, false, 0.40f, 0f, true ),
    LAVA  (new Color(220,  80,  30), true,  false, 0.30f, 5f, false);

    private final Color   color;
    private final boolean walkable;
    private final boolean solid;
    private final float   speedFactor;
    private final float   damagePerSecond;
    private final boolean causesDrowning;

    TileMaterial(final Color color, final boolean walkable, final boolean solid,
                 final float speedFactor, final float damagePerSecond,
                 final boolean causesDrowning) {
        this.color = color;
        this.walkable = walkable;
        this.solid = solid;
        this.speedFactor = speedFactor;
        this.damagePerSecond = damagePerSecond;
        this.causesDrowning = causesDrowning;
    }

    public Color   getColor()           { return color; }
    public boolean isWalkable()         { return walkable; }
    public boolean isSolid()            { return solid; }
    public float   getSpeedFactor()     { return speedFactor; }
    public float   getDamagePerSecond() { return damagePerSecond; }
    public boolean causesDrowning()     { return causesDrowning; }

    /** Stable id used by {@link MaterialRegistry} and serialized files (e.g. {@code "stone"}). */
    public String materialId() {
        return name().toLowerCase();
    }

    /** Builds the canonical {@link Material} record for this enum value. */
    public Material toMaterial() {
        return new Material(
                materialId(),
                displayName(),
                solid,
                damagePerSecond,
                speedFactor,
                Map.of(
                        "walkable",       walkable,
                        "causesDrowning", causesDrowning));
    }

    private String displayName() {
        final String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

