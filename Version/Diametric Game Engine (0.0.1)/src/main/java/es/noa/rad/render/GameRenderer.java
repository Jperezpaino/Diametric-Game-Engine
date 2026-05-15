package es.noa.rad.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import es.noa.rad.camera.Camera;
import es.noa.rad.config.GameConfig;
import es.noa.rad.entity.Player;
import es.noa.rad.map.TileMap;
import es.noa.rad.projection.IsoProjection;

/**
 * High-level renderer that orchestrates every drawing step of a frame.
 *
 * <p>Responsibilities:</p>
 * <ol>
 *   <li>Clear the back buffer.</li>
 *   <li>Apply the camera translation.</li>
 *   <li>Render tiles and entities in the correct order.</li>
 *   <li>Draw debug information (FPS / UPS) in screen space.</li>
 * </ol>
 *
 * @since Phase 1
 */
public final class GameRenderer {

    private static final Color BACKGROUND = new Color(20, 20, 30);
    private static final Color HUD_COLOR  = Color.WHITE;
    private static final Font  HUD_FONT   = new Font("Monospaced", Font.PLAIN, 14);

    private final TileRenderer   tileRenderer;
    private final EntityRenderer entityRenderer;

    public GameRenderer(final IsoProjection projection) {
        this.tileRenderer   = new TileRenderer(projection);
        this.entityRenderer = new EntityRenderer(projection);
    }

    /**
     * Renders a full frame.
     *
     * @param g      graphics context of the active back buffer
     * @param map    tile map
     * @param player player entity
     * @param camera camera
     * @param fps    measured frames per second
     * @param ups    measured updates per second
     */
    public void render(final Graphics2D g, final TileMap map, final Player player,
                       final Camera camera, final int fps, final int ups) {
        // Clear background.
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // World rendering with camera applied.
        final java.awt.geom.AffineTransform original = g.getTransform();
        camera.apply(g);
        tileRenderer.render(g, map);
        entityRenderer.render(g, player);
        g.setTransform(original);

        // HUD (screen space).
        g.setColor(HUD_COLOR);
        g.setFont(HUD_FONT);
        g.drawString("FPS: " + fps + "  UPS: " + ups, 10, 20);
    }
}
