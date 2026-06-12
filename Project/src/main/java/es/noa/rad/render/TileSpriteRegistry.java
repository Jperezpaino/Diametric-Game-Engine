package es.noa.rad.render;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import es.noa.rad.map.TileMaterial;
import es.noa.rad.map.TileShape;

/**
 * Registry of tile sprite PNG assets, keyed by {@code (TileMaterial, TileShape)}.
 *
 * <p>At construction time it probes the classpath for
 * {@code tiles/<material>_<shape>.png} (lowercase) for every enum combination
 * and caches the {@link BufferedImage} when found. Missing files are recorded
 * as {@code null} so the renderer can fall back to its polygon placeholder
 * without re-checking the filesystem on every frame.</p>
 *
 * <p>Convention for asset authors:</p>
 * <ul>
 *   <li>Bottom-center of the PNG aligns with the south apex of the tile
 *       diamond at the tile's base elevation.</li>
 *   <li>Width = {@code TILE_WIDTH} px (the diamond's east-west extent).</li>
 *   <li>Height = top face + total side height (varies with elevation).</li>
 * </ul>
 *
 * @since Phase 7b
 */
public final class TileSpriteRegistry {

    private final Map<TileMaterial, Map<TileShape, BufferedImage>> cache;

    public TileSpriteRegistry() {
        this.cache = new EnumMap<>(TileMaterial.class);
        for (final TileMaterial mat : TileMaterial.values()) {
            final EnumMap<TileShape, BufferedImage> perShape = new EnumMap<>(TileShape.class);
            cache.put(mat, perShape);
            for (final TileShape shape : TileShape.values()) {
                perShape.put(shape, AssetImages.tryLoad(resourcePath(mat, shape)));
            }
        }
    }

    /** Returns the cached sprite for the pair, or {@code null} if no PNG is available. */
    public BufferedImage get(final TileMaterial material, final TileShape shape) {
        if (material == null || shape == null) return null;
        final Map<TileShape, BufferedImage> perShape = cache.get(material);
        return perShape == null ? null : perShape.get(shape);
    }

    private static String resourcePath(final TileMaterial mat, final TileShape shape) {
        return "tiles/" + mat.name().toLowerCase() + "_" + shape.name().toLowerCase() + ".png";
    }
}
