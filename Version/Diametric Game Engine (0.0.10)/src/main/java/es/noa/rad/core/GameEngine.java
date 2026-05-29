package es.noa.rad.core;

import java.awt.Graphics2D;
import java.io.IOException;

import es.noa.rad.camera.Camera;
import es.noa.rad.config.GameConfig;
import es.noa.rad.entity.Player;
import es.noa.rad.input.InputHandler;
import es.noa.rad.input.InputState;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.io.MapLoader;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.WorldPoint;
import es.noa.rad.render.GameRenderer;

/**
 * Top-level engine orchestrator.
 *
 * <p>Owns every long-lived component (window, projection, map, player,
 * camera, input, renderer and game loop) and exposes the {@code update} and
 * {@code render} hooks used by the loop.</p>
 *
 * @since Phase 1 (input wired in Phase 2)
 */
public final class GameEngine {

    private GameWindow    window;
    private GameLoop      loop;
    private GameRenderer  renderer;
    private IsoProjection projection;

    private TileMap    map;
    private Player     player;
    private Camera     camera;
    private InputState input;

    private GameState state = GameState.LOADING;

    /** Initialises every subsystem and starts the loop. */
    public void start() {
        this.window     = new GameWindow();
        this.projection = new IsoProjection();
        this.map        = loadInitialMap();
        this.player     = new Player();
        this.camera     = new Camera();
        this.renderer   = new GameRenderer(projection);
        this.input      = new InputState();

        // Wire input into the player.
        player.init(input, map);

        // Place the player on the central cell of the loaded map, snapped
        // to the surface of the top tile underneath.
        final int[] center = MapLoader.centerCell(map);
        final WorldPoint pos = player.getPosition();
        pos.setCol(center[0]);
        pos.setRow(center[1]);
        pos.setZ(MapLoader.surfaceElevation(map, center[0], center[1]));

        // Register keyboard listener on the canvas.
        window.getCanvas().addKeyListener(new InputHandler(input));
        window.getCanvas().requestFocusInWindow();

        // Initial camera centring (before loop starts; no deltaTime/input yet).
        camera.centreOn(player.getPosition(), projection);

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
        camera.update(deltaTime, input, player.getPosition(), map, projection);
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

    /**
     * Loads the starting map from {@code map/map_1.json}. If the resource is
     * missing or malformed we fall back to {@link TileMap#createDemoMap()}
     * so the engine still boots and the failure is visible in the console.
     */
    private static TileMap loadInitialMap() {
        final String resource = GameConfig.STARTUP_MAP;
        try {
            final TileMap loaded = MapLoader.loadFromClasspath(resource);
            System.out.println("[GameEngine] Loaded map '" + resource + "' ("
                + loaded.getWidth() + "x" + loaded.getDepth() + ").");
            return loaded;
        } catch (final IOException ex) {
            System.err.println("[GameEngine] Could not load " + resource
                + " (" + ex.getMessage() + "); using built-in demo map.");
            return TileMap.createDemoMap();
        } catch (final RuntimeException ex) {
            System.err.println("[GameEngine] Malformed " + resource
                + " (" + ex.getMessage() + "); using built-in demo map.");
            ex.printStackTrace(System.err);
            return TileMap.createDemoMap();
        }
    }
}
