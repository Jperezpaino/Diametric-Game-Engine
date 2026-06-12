package es.noa.rad.map;

/**
 * One voxel of the world. Combines a {@link TileMaterial} (gameplay layer),
 * a {@link TileShape} (top-surface geometry) and an integer {@code elevation}
 * (Z slot inside its containing {@link TileLayer}).
 *
 * @since Phase 3
 */
public record Tile(TileMaterial material, TileShape shape, int elevation) {

    public Tile(final TileMaterial material, final TileShape shape) {
        this(material, shape, 0);
    }

    public int absoluteEdgeHeight(final EdgeSide side) {
        return elevation + shape.edgeHeight(side);
    }

    public float absoluteCornerHeight(final TileShape.Corner corner) {
        return elevation + shape.cornerHeight(corner);
    }

    public boolean isMaterialWalkable() {
        return material.isWalkable();
    }
}