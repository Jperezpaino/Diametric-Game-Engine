package es.noa.rad.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * One voxel of the world. Holds a reference to its {@link TileDefinition}
 * (the {@code Skin} + {@code Material} + {@code TileShape} recipe) and an
 * integer {@code elevation} (Z slot inside its containing {@link TileLayer}).
 *
 * <p>Phase 8d-bis flipped the runtime so {@code Tile} carries a definition
 * reference instead of the legacy {@code (TileMaterial, TileShape)} pair.
 * Legacy accessors {@link #material()} and {@link #shape()} keep returning
 * the enum types so every existing consumer (renderers, pathfinder, terrain
 * effects, map I/O) keeps working without changes. The convenience
 * constructor {@link #Tile(TileMaterial, TileShape, int)} resolves the
 * canonical definition through {@link TileDefinitionRegistry#DEFAULT}.</p>
 *
 * @since Phase 3 (flipped to definition reference in Phase 8d-bis)
 */
public record Tile(TileDefinition definition, int elevation) {

    private static final Map<String, TileMaterial> MATERIAL_BY_ID = buildMaterialIndex();

    public Tile {
        Objects.requireNonNull(definition, "definition");
    }

    /** Builds a tile from the legacy enum pair via {@link TileDefinitionRegistry#DEFAULT}. */
    public Tile(final TileMaterial material, final TileShape shape, final int elevation) {
        this(resolveDefault(material, shape), elevation);
    }

    /** Builds a tile at elevation 0 from the legacy enum pair. */
    public Tile(final TileMaterial material, final TileShape shape) {
        this(material, shape, 0);
    }

    // -------------------------------------------------------------------------
    // Legacy accessors (forwarded to the underlying definition).
    // -------------------------------------------------------------------------

    /** Resolves the legacy {@link TileMaterial} enum value backing this tile. */
    public TileMaterial material() {
        return MATERIAL_BY_ID.get(definition.materialId());
    }

    /** {@link TileShape} of the tile's top face. */
    public TileShape shape() {
        return definition.shape();
    }

    /** Stable id of the {@link TileDefinition} this tile points to. */
    public String definitionId() {
        return definition.id();
    }

    public int absoluteEdgeHeight(final EdgeSide side) {
        return elevation + shape().edgeHeight(side);
    }

    public float absoluteCornerHeight(final TileShape.Corner corner) {
        return elevation + shape().cornerHeight(corner);
    }

    public boolean isMaterialWalkable() {
        return material().isWalkable();
    }

    // -------------------------------------------------------------------------
    // Internals.
    // -------------------------------------------------------------------------

    private static TileDefinition resolveDefault(final TileMaterial material, final TileShape shape) {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(shape,    "shape");
        final String id = TileDefinitionRegistry.defaultId(material, shape);
        final TileDefinition def = TileDefinitionRegistry.DEFAULT.get(id);
        if (def == null) {
            throw new IllegalStateException("No default TileDefinition registered for id '" + id + "'");
        }
        return def;
    }

    private static Map<String, TileMaterial> buildMaterialIndex() {
        final Map<String, TileMaterial> index = new HashMap<>();
        for (final TileMaterial tm : TileMaterial.values()) {
            index.put(tm.materialId(), tm);
        }
        return Map.copyOf(index);
    }
}
