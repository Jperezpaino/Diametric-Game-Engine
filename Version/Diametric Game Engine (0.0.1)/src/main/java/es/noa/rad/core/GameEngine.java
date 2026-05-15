package es.noa.rad.core;

import java.awt.Graphics2D;

import es.noa.rad.camera.Camera;
import es.noa.rad.entity.Player;
import es.noa.rad.map.TileMap;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.render.GameRenderer;

/**
 * Top-level engine orchestrator.
 *
 * <p>Owns every long-lived component of Phase 1 (window, projection, map,
 * player, camera, renderer and game loop) and exposes the {@code update} and
 * {@code render} hooks used by the loop.</p>
 *
 * @since Phase 1
 */
public final class GameEngine {

    private GameWindow   window;
    private GameLoop     loop;
    private GameRenderer renderer;
    private IsoProjection projection;

    private TileMap map;
    private Player  player;
    private Camera  camera;

    private GameState state = GameState.LOADING;

    /** Initialises every subsystem and starts the loop. */
    public void start() {
        this.window     = new GameWindow();
        this.projection = new IsoProjection();
        this.map        = TileMap.createDemoMap();
        this.player     = new Player();
        this.camera     = new Camera();
        this.renderer   = new GameRenderer(projection);

        // Initial camera centring.
        camera.update(player.getPosition(), projection);

        this.loop  = new GameLoop(this);
        this.state = GameState.RUNNING;
        loop.start();
    }

    /**
     * Updates game logic for the elapsed time slice.
     *
     * @param deltaTime elapsed seconds since the previous update
     */
    public void update(final double deltaTime) {
        if (state != GameState.RUNNING) {
            return;
        }
        player.update(deltaTime);
        camera.update(player.getPosition(), projection);
    }

    /** Renders the current frame. */
    public void render() {
        final Graphics2D g = window.getGraphics2D();
        try {
            renderer.render(g, map, player, camera, loop.getCurrentFps(), loop.getCurrentUps());
        } finally {
            g.dispose();
        }
        window.show();
    }

    public GameState getState() { return state; }
    public void setState(final GameState state) { this.state = state; }
}
