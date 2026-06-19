package es.noa.rad.render;

import java.awt.image.BufferedImage;

import es.noa.rad.map.TileMaterial;
import es.noa.rad.map.TileShape;

/**
 * Adapter that resolves a tile sprite from a {@code (TileMaterial, TileShape)}
 * pair via the underlying {@link SkinRegistry}.
 *
 * <p>Originally this class owned its own sprite cache keyed on the enum pair
 * (Phase 7b). Phase 8a introduced {@link es.noa.rad.map.Skin} as the
 * canonical visual abstraction and {@link SkinRegistry} as the single source
 * of truth for image assets, so this registry is now a thin translation layer
 * that maps the legacy enum pair to a synthetic skin id
 * ({@link SkinRegistry#defaultSkinId}).</p>
 *
 * <p>Once Phase 8d migrates tiles to reference an explicit {@code skinId} this
 * adapter can be deleted in favour of a direct {@code SkinRegistry} lookup.</p>
 *
 * @since Phase 7b (rewritten Phase 8a)
 */
public final class TileSpriteRegistry {

    private final SkinRegistry skins;

    public TileSpriteRegistry() {
        this(SkinRegistry.loadDefaults());
    }

    public TileSpriteRegistry(final SkinRegistry skins) {
        this.skins = skins;
    }

    /** Returns the cached sprite for the pair, or {@code null} if no PNG is available. */
    public BufferedImage get(final TileMaterial material, final TileShape shape) {
        if (material == null || shape == null) return null;
        return skins.getImage(SkinRegistry.defaultSkinId(material, shape));
    }

    /** Exposes the underlying skin registry for callers that already work in skin-id space. */
    public SkinRegistry skins() {
        return skins;
    }
}
