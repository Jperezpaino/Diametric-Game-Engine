package es.noa.rad.map;

/**
 * Catalogue of the 14 geometric shapes a tile can take in the diametric
 * world. A shape is fully described by the binary height of its four
 * cardinal edges (N, E, S, W); each edge is either {@code 0} (low, at the
 * tile&#39;s base z) or {@code 1} (high, raised one unit).
 *
 * <p>Shape encoding uses a 4-bit mask: {@code N=8, E=4, S=2, W=1}.</p>
 *
 * <p>Naming convention for ramps:</p>
 * <ul>
 *   <li><b>RAMP_*</b> — two adjacent edges high, slope falls toward the
 *       opposite corner (e.g. {@code RAMP_SW} has N+E edges high, slope down
 *       to the SW corner).</li>
 *   <li><b>DOUBLE_RAMP_*</b> — only one edge high (a ridge along that edge
 *       producing two slopes in opposite corners). The suffix is the high
 *       edge.</li>
 *   <li><b>CONCAVE_*</b> — three edges high, one low. The suffix is the low
 *       edge.</li>
 *   <li><b>FLOOR</b> — all four edges low (flat ground).</li>
 *   <li><b>BLOCK</b> — all four edges high (solid cube; navigable on top).</li>
 * </ul>
 *
 * <p>The 14 shapes correspond exactly to the reference assets supplied in
 * {@code doc/Info/Reference.json} (with the geometric correction noted in
 * {@code PHASE_03.md} for the 4th double ramp).</p>
 *
 * <h3>Equivalence with the Map System spec</h3>
 * <p>The Map System spec lists a minimal example set
 * ({@code FLAT, WALL, SLOPE_NORTH/SOUTH/EAST/WEST, STAIR_UP/DOWN}) but does
 * not enforce it as exhaustive. This engine uses a richer, more descriptive
 * vocabulary captured by this enum. The mapping for the spec's examples is:</p>
 * <ul>
 *   <li>{@code FLAT}         &harr; {@link #FLOOR}</li>
 *   <li>{@code WALL}         &harr; {@link #BLOCK}</li>
 *   <li>{@code SLOPE_NORTH}  &harr; {@link #DOUBLE_RAMP_N}</li>
 *   <li>{@code SLOPE_EAST}   &harr; {@link #DOUBLE_RAMP_E}</li>
 *   <li>{@code SLOPE_SOUTH}  &harr; {@link #DOUBLE_RAMP_S}</li>
 *   <li>{@code SLOPE_WEST}   &harr; {@link #DOUBLE_RAMP_W}</li>
 *   <li>{@code STAIR_UP/DOWN} &mdash; not modelled as separate shapes; built
 *       from chained {@code DOUBLE_RAMP_*} tiles in adjacent voxels.</li>
 * </ul>
 * <p>The {@code RAMP_*} (corner-pointing) and {@code CONCAVE_*} families have
 * no equivalent in the spec's example set &mdash; they are engine-specific
 * extensions.</p>
 *
 * <p>Editor tooling and serialized files use {@link #shapeId()} (lowercase
 * enum name, e.g. {@code "floor"}, {@code "ramp_ne"}, {@code "double_ramp_e"})
 * as the canonical id. {@link #displayName()} provides a human-friendly
 * label (e.g. {@code "Ramp NE"}, {@code "Double Ramp E"}) for UI dropdowns.</p>
 *
 * @since Phase 3 (corner-height redesign)
 */
public enum TileShape {

    // -----------------------------------------------------------------------
    // Flat ground.
    // -----------------------------------------------------------------------
    FLOOR             (0b0000),

    // -----------------------------------------------------------------------
    // Solid block (cube). All edges raised; transitable only on the top face.
    // -----------------------------------------------------------------------
    BLOCK             (0b1111),

    // -----------------------------------------------------------------------
    // Double ramps — single high edge / ridge.
    // -----------------------------------------------------------------------
    DOUBLE_RAMP_N     (0b1000),  // N edge high  (ridge along north)
    DOUBLE_RAMP_E     (0b0100),  // E edge high
    DOUBLE_RAMP_S     (0b0010),  // S edge high
    DOUBLE_RAMP_W     (0b0001),  // W edge high

    // -----------------------------------------------------------------------
    // Single ramps — two adjacent high edges, slope to opposite corner.
    // -----------------------------------------------------------------------
    RAMP_SW           (0b1100),  // N + E high → slope to SW corner
    RAMP_NW           (0b0110),  // E + S high → slope to NW corner
    RAMP_NE           (0b0011),  // S + W high → slope to NE corner
    RAMP_SE           (0b1001),  // W + N high → slope to SE corner

    // -----------------------------------------------------------------------
    // Concave slopes — three high edges and one low (the suffix).
    // -----------------------------------------------------------------------
    CONCAVE_N         (0b0111),  // N low (E+S+W high)
    CONCAVE_E         (0b1011),  // E low (N+S+W high)
    CONCAVE_S         (0b1101),  // S low (N+E+W high)
    CONCAVE_W         (0b1110);  // W low (N+E+S high)

    private static final int BIT_N = 0b1000;
    private static final int BIT_E = 0b0100;
    private static final int BIT_S = 0b0010;
    private static final int BIT_W = 0b0001;

    private final int edgeMask;

    TileShape(final int edgeMask) {
        this.edgeMask = edgeMask;
    }

    /** @return the 4-bit edge mask (N=8, E=4, S=2, W=1). */
    public int edgeMask() { return edgeMask; }

    /**
     * @param side cardinal edge to query
     * @return 0 (low) or 1 (high) for that edge
     */
    public int edgeHeight(final EdgeSide side) {
        switch (side) {
            case NORTH: return (edgeMask & BIT_N) != 0 ? 1 : 0;
            case EAST:  return (edgeMask & BIT_E) != 0 ? 1 : 0;
            case SOUTH: return (edgeMask & BIT_S) != 0 ? 1 : 0;
            case WEST:  return (edgeMask & BIT_W) != 0 ? 1 : 0;
            default: throw new IllegalStateException("Unknown side: " + side);
        }
    }

    /**
     * Height (0 or 1) of one of the four diamond-tip corners of the top face.
     *
     * <p>The four bits of the edge mask are NOT the heights of the tile's four
     * polygon sides; they are the heights of the four <em>tip corners</em> of
     * the screen diamond, named after the cardinal direction each tip points
     * toward on screen:</p>
     * <pre>
     *   N bit (0b1000) → Corner.NW  (the top    screen tip)
     *   E bit (0b0100) → Corner.NE  (the right  screen tip)
     *   S bit (0b0010) → Corner.SE  (the bottom screen tip)
     *   W bit (0b0001) → Corner.SW  (the left   screen tip)
     * </pre>
     *
     * <p>Consequently, the per-family counts are exactly:</p>
     * <ul>
     *   <li>FLOOR        — 0 elevated corners</li>
     *   <li>DOUBLE_RAMP  — 1 elevated corner  (the single high tip)</li>
     *   <li>RAMP         — 2 elevated corners (two adjacent tips)</li>
     *   <li>CONCAVE      — 3 elevated corners (three tips)</li>
     *   <li>BLOCK        — 4 elevated corners</li>
     * </ul>
     *
     * @param corner one of {@link Corner}
     * @return 0 or 1
     */
    public float cornerHeight(final Corner corner) {
        switch (corner) {
            case NW: return edgeHeight(EdgeSide.NORTH);
            case NE: return edgeHeight(EdgeSide.EAST);
            case SE: return edgeHeight(EdgeSide.SOUTH);
            case SW: return edgeHeight(EdgeSide.WEST);
            default: throw new IllegalStateException("Unknown corner: " + corner);
        }
    }

    /** @return {@code true} if all four edges are at 0. */
    public boolean isFlat()  { return edgeMask == 0; }
    /** @return {@code true} if all four edges are at 1 (BLOCK). */
    public boolean isBlock() { return edgeMask == 0b1111; }
    /** @return {@code true} for the 4 RAMP_* shapes (exactly two adjacent edges high). */
    public boolean isRamp()  {
        return Integer.bitCount(edgeMask) == 2 && edgeMask != 0b1010 && edgeMask != 0b0101;
    }
    /** @return {@code true} for the 4 DOUBLE_RAMP_* shapes (exactly one edge high). */
    public boolean isDoubleRamp() { return Integer.bitCount(edgeMask) == 1; }
    /** @return {@code true} for the 4 CONCAVE_* shapes (exactly three edges high). */
    public boolean isConcave()    { return Integer.bitCount(edgeMask) == 3; }

    /** @return {@code true} if the top face is not flat (any sloped surface). */
    public boolean isSloped() { return edgeMask != 0 && edgeMask != 0b1111; }

    /**
     * Stable id used by editor tooling and serialized files
     * (lowercase enum name, e.g. {@code "floor"}, {@code "ramp_ne"},
     * {@code "double_ramp_e"}, {@code "concave_s"}).
     */
    public String shapeId() {
        return name().toLowerCase();
    }

    /**
     * Human-friendly label for UI dropdowns (e.g. {@code "Floor"},
     * {@code "Ramp NE"}, {@code "Double Ramp E"}, {@code "Concave S"}).
     */
    public String displayName() {
        final String[] parts = name().toLowerCase().split("_");
        final StringBuilder sb = new StringBuilder(name().length());
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(' ');
            final String p = parts[i];
            if (p.length() <= 2) {
                sb.append(p.toUpperCase()); // cardinal suffixes (n, e, s, w, ne, …)
            } else {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }

    /** Diamond corners of the tile&#39;s top face. */
    public enum Corner { NE, SE, SW, NW }
}
