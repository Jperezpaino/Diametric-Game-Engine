package es.noa.rad.map;

import java.awt.Color;

/**
 * Catalogue of tile types. Each value carries a colour, a walkable flag and
 * the relative height (0 or 1) of its four diamond corners (N, E, S, W) on
 * top of {@link Tile#getElevation()}. Flat tiles use (0,0,0,0); ramps raise
 * one or two corners to model an inclined surface.
 *
 * @since Phase 1 (corner-height ramps added Phase 3)
 */
public enum TileType {

    GRASS    (new Color(80, 170, 80),   true,  0, 0, 0, 0),
    WATER    (new Color(60, 110, 200),  false, 0, 0, 0, 0),
    WALL     (new Color(120, 120, 120), false, 0, 0, 0, 0),
    FLOOR    (new Color(190, 190, 190), true,  0, 0, 0, 0),
    ELEVATED (new Color(110, 75, 45),   true,  0, 0, 0, 0),
    CLIFF    (new Color(70, 70, 70),    false, 0, 0, 0, 0),

    RAMP_NW  (new Color(220, 150, 60),  true,  1, 0, 0, 1),
    RAMP_NE  (new Color(220, 150, 60),  true,  1, 1, 0, 0),
    RAMP_SW  (new Color(220, 150, 60),  true,  0, 0, 1, 1),
    RAMP_SE  (new Color(220, 150, 60),  true,  0, 1, 1, 0),

    RAMP_N   (new Color(230, 170, 80),  true,  1, 0, 0, 0),
    RAMP_S   (new Color(230, 170, 80),  true,  0, 0, 1, 0),
    RAMP_W   (new Color(230, 170, 80),  true,  0, 0, 0, 1),
    RAMP_E   (new Color(230, 170, 80),  true,  0, 1, 0, 0);

    public static final int N = 0;
    public static final int E = 1;
    public static final int S = 2;
    public static final int W = 3;

    private final Color color;
    private final boolean walkable;
    private final int cornerN;
    private final int cornerE;
    private final int cornerS;
    private final int cornerW;

    TileType(final Color color, final boolean walkable,
             final int cornerN, final int cornerE,
             final int cornerS, final int cornerW) {
        this.color = color;
        this.walkable = walkable;
        this.cornerN = cornerN;
        this.cornerE = cornerE;
        this.cornerS = cornerS;
        this.cornerW = cornerW;
    }

    public Color   getColor()   { return color; }
    public boolean isWalkable() { return walkable; }

    public int getCornerHeight(final int corner) {
        switch (corner) {
            case N: return cornerN;
            case E: return cornerE;
            case S: return cornerS;
            case W: return cornerW;
            default: throw new IllegalArgumentException("Invalid corner: " + corner);
        }
    }

    public int[] getCornerHeights() {
        return new int[] { cornerN, cornerE, cornerS, cornerW };
    }

    public int getMaxCornerHeight() {
        return Math.max(Math.max(cornerN, cornerE), Math.max(cornerS, cornerW));
    }

    public boolean isRamp() {
        final int min = Math.min(Math.min(cornerN, cornerE), Math.min(cornerS, cornerW));
        return getMaxCornerHeight() != min;
    }
}
