package es.noa.rad.render;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import es.noa.rad.map.Skin;
import es.noa.rad.map.TileMaterial;
import es.noa.rad.map.TileShape;

/**
 * Single source of truth for tile skin assets.
 *
 * <p>A {@link Skin} is the visual half of a tile (id, display name, classpath
 * image reference). The registry owns the catalogue of skins and the cached
 * {@link BufferedImage} loaded for each one. Anything that needs to draw a
 * tile resolves its sprite through this registry instead of re-loading PNGs
 * on its own.</p>
 *
 * <p>Loading uses {@link AssetImages#tryLoad(String)} so the magenta colour
 * key is honoured uniformly. When an image cannot be loaded the entry is
 * still registered with a {@code null} cached image so the renderer can
 * fall back to its polygon placeholder without re-probing the classpath
 * every frame.</p>
 *
 * <h3>Default skin set (Phase 8a)</h3>
 * <p>Until Tile Builder writes real {@code .skins} files we seed the registry
 * from every {@code (TileMaterial, TileShape)} combination, mirroring the
 * naming convention introduced in Phase 7b
 * ({@code tiles/<material>_<shape>.png}). The synthetic id is
 * {@code "<material>_<shape>"} in lowercase. Phase 8d will replace these
 * synthetic ids with explicit ones referenced from {@code TileDefinition}.</p>
 *
 * @since Phase 8a
 */
public final class SkinRegistry {

    private final Map<String, Skin>          skins  = new LinkedHashMap<>();
    private final Map<String, BufferedImage> images = new LinkedHashMap<>();

    /** Adds a skin to the registry and eagerly loads its image (may end up {@code null}). */
    public void register(final Skin skin) {
        skins.put(skin.id(), skin);
        images.put(skin.id(), AssetImages.tryLoad(skin.imageRef()));
    }

    /** Returns the {@link Skin} registered under {@code id}, or {@code null} if unknown. */
    public Skin getSkin(final String id) {
        return id == null ? null : skins.get(id);
    }

    /** Returns the cached image for {@code id}, or {@code null} if missing or not loaded. */
    public BufferedImage getImage(final String id) {
        return id == null ? null : images.get(id);
    }

    /** Read-only view of every registered skin (insertion order preserved). */
    public Collection<Skin> skins() {
        return skins.values();
    }

    /**
     * Builds the default skin set used while no editor-authored {@code .skins}
     * file exists yet. One skin per {@code (material, shape)} pair, with id
     * {@code "<material>_<shape>"} (lowercase) and {@code imageRef}
     * {@code "tiles/<id>.png"}.
     */
    public static SkinRegistry loadDefaults() {
        final SkinRegistry registry = new SkinRegistry();
        for (final TileMaterial material : TileMaterial.values()) {
            for (final TileShape shape : TileShape.values()) {
                final String id        = defaultSkinId(material, shape);
                final String imageRef  = "tiles/" + id + ".png";
                final String name      = displayName(material, shape);
                registry.register(new Skin(id, name, imageRef));
            }
        }
        return registry;
    }

    /** Synthetic skin id derived from a {@code (material, shape)} pair. */
    public static String defaultSkinId(final TileMaterial material, final TileShape shape) {
        return material.name().toLowerCase() + "_" + shape.name().toLowerCase();
    }

    private static String displayName(final TileMaterial material, final TileShape shape) {
        return capitalize(material.name()) + " " + capitalize(shape.name().replace('_', ' '));
    }

    private static String capitalize(final String raw) {
        if (raw.isEmpty()) return raw;
        final String lower = raw.toLowerCase();
        final StringBuilder sb = new StringBuilder(lower.length());
        boolean upper = true;
        for (int i = 0; i < lower.length(); i++) {
            final char c = lower.charAt(i);
            if (c == ' ') { sb.append(c); upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }
}
