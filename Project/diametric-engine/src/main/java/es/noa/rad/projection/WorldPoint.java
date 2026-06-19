package es.noa.rad.projection;

/**
 * Immutable point in world space.
 *
 * <p>World coordinates are expressed in tile units:</p>
 * <ul>
 *   <li>{@code col} → column index (X axis on the grid).</li>
 *   <li>{@code row} → row index (Y axis on the grid, depth).</li>
 *   <li>{@code z}   → height above the ground plane in tile units.</li>
 * </ul>
 *
 * <p>Decimals are allowed so an entity can sit in the middle of a cell.</p>
 *
 * @since Phase 1
 */
public final class WorldPoint {

    private float col;
    private float row;
    private float z;

    /**
     * Creates a world point.
     *
     * @param col column (X) in tile units
     * @param row row (Y) in tile units
     * @param z   height in tile units
     */
    public WorldPoint(final float col, final float row, final float z) {
        this.col = col;
        this.row = row;
        this.z = z;
    }

    public float getCol() { return col; }
    public float getRow() { return row; }
    public float getZ()   { return z; }

    public void setCol(final float col) { this.col = col; }
    public void setRow(final float row) { this.row = row; }
    public void setZ(final float z)     { this.z = z; }

    @Override
    public String toString() {
        return "WorldPoint(col=" + col + ", row=" + row + ", z=" + z + ")";
    }
}
