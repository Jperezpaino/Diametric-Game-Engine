package es.noa.rad.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

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

    /** Color key treated as transparent when present in a tile PNG (magenta). */
    private static final int COLOR_KEY_RGB = 0x00FF00FF;

    private final Map<TileMaterial, Map<TileShape, BufferedImage>> cache;

    public TileSpriteRegistry() {
        this.cache = new EnumMap<>(TileMaterial.class);
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (final TileMaterial mat : TileMaterial.values()) {
            final EnumMap<TileShape, BufferedImage> perShape = new EnumMap<>(TileShape.class);
            cache.put(mat, perShape);
            for (final TileShape shape : TileShape.values()) {
                perShape.put(shape, tryLoad(cl, resourcePath(mat, shape)));
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

    private static BufferedImage tryLoad(final ClassLoader cl, final String path) {
        try (InputStream in = cl.getResourceAsStream(path)) {
            if (in == null) return null;
            final BufferedImage raw = ImageIO.read(in);
            return raw == null ? null : applyMagentaKey(raw);
        } catch (IOException ex) {
            System.err.println("[TileSpriteRegistry] failed to read " + path + ": " + ex.getMessage());
            return null;
        }
    }

    /**
     * Returns an ARGB copy of {@code src} where every pixel matching the
     * magenta color key {@code RGB(255,0,255)} is fully transparent. Pixels
     * are compared on the RGB channels only, ignoring the source alpha so
     * the convention works for both opaque PNGs and pre-keyed exports.
     */
    private static BufferedImage applyMagentaKey(final BufferedImage src) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        final BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = out.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((out.getRGB(x, y) & 0x00FFFFFF) == (COLOR_KEY_RGB & 0x00FFFFFF)) {
                    out.setRGB(x, y, 0x00000000);
                }
            }
        }
        return out;
    }
}
