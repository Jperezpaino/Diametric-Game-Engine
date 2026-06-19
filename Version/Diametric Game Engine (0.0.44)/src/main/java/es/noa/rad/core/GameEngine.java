package es.noa.rad.core;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import es.noa.rad.audio.SoundId;
import es.noa.rad.audio.SoundManager;
import es.noa.rad.camera.Camera;
import es.noa.rad.config.GameConfig;
import es.noa.rad.entity.Player;
import es.noa.rad.input.InputHandler;
import es.noa.rad.input.InputState;
import es.noa.rad.input.MouseInput;
import es.noa.rad.map.TileMap;
import es.noa.rad.map.MapResource;
import es.noa.rad.map.MaterialRegistry;
import es.noa.rad.map.Structure;
import es.noa.rad.map.TileDefinitionRegistry;
import es.noa.rad.map.TileInstance;
import es.noa.rad.map.io.MapLoader;
import es.noa.rad.map.io.MapWriter;
import es.noa.rad.map.io.MaterialSetIO;
import es.noa.rad.map.io.SkinSetIO;
import es.noa.rad.map.io.StructureSetIO;
import es.noa.rad.map.io.TileSetIO;
import es.noa.rad.pathfinding.PathFollower;
import es.noa.rad.pathfinding.PathNode;
import es.noa.rad.pathfinding.Pathfinder;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;
import es.noa.rad.projection.WorldPoint;
import es.noa.rad.render.GameRenderer;
import es.noa.rad.render.SkinRegistry;
import es.noa.rad.render.animation.AnimationController;
import es.noa.rad.render.animation.AnimationState;
import es.noa.rad.render.animation.EntitySprites;
import es.noa.rad.render.animation.PlaceholderSprite;
import es.noa.rad.ui.dialog.Dialog;
import es.noa.rad.ui.dialog.DialogManager;

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

    /** Upper bound for elevation scan when picking a cell from a mouse click. */
    private static final int MAX_PICK_ELEVATION = 16;

    private GameWindow    window;
    private GameLoop      loop;
    private GameRenderer  renderer;
    private IsoProjection projection;

    private TileMap       map;
    private Player        player;
    private Camera        camera;
    private InputState    input;
    private MouseInput    mouse;
    private Pathfinder    pathfinder;
    private PathFollower  follower;
    private SoundManager  sound;
    private DialogManager dialogs;

    /** Debug overlay path (Phase 4c). Filled by pressing {@code P}. */
    private List<PathNode> debugPath = Collections.emptyList();
    private boolean prevPathDown;
    private boolean pathOverlayActive;

    /** Phase 6a audio state. */
    private float   footstepAccum;
    private float   prevCol;
    private float   prevRow;
    private float   prevHp;
    private boolean prevAlive   = true;
    private boolean prevFollowerActive;
    private float   hurtCooldown;
    private static final float FOOTSTEP_DISTANCE = 0.55f;
    private static final float HURT_COOLDOWN_SEC = 0.45f;

    /** Phase 6d dialog state (edge-trigger tracking). */
    private boolean prevDialogOpenKey;
    private boolean prevDialogAdvanceKey;

    /** Phase 7a save-on-F5 state (edge trigger). */
    private boolean prevSaveKey;
    private static final Path SAVE_DIR = Paths.get("saves");

    /** Phase 7d runtime toggles (edge-triggered). */
    private boolean prevTextureToggleKey;
    private boolean prevMuteToggleKey;

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
        this.mouse      = new MouseInput();
        this.pathfinder = new Pathfinder(map.getLayer());
        this.follower   = new PathFollower();
        this.sound      = new SoundManager();
        this.dialogs    = new DialogManager();

        // Wire input into the player.
        player.init(input, map);

        // Phase 5: attach a placeholder animation controller so the player
        // is drawn as a directional sprite instead of the diamond marker.
        // Phase 7c: prefer a real PNG atlas at entities/player.png when present.
        AnimationController playerAnim = EntitySprites.tryLoadController("player");
        if (playerAnim == null) playerAnim = PlaceholderSprite.createController();
        player.setAnimationController(playerAnim);

        // Place the player on the central cell of the loaded map, snapped
        // to the surface of the top tile underneath.
        final int[] center = MapLoader.centerCell(map);
        final WorldPoint pos = player.getPosition();
        pos.setCol(center[0]);
        pos.setRow(center[1]);
        pos.setZ(MapLoader.surfaceElevation(map, center[0], center[1]));

        // Register input listeners on the canvas.
        window.getCanvas().addKeyListener(new InputHandler(input));
        window.getCanvas().addMouseListener(mouse);
        window.getCanvas().requestFocusInWindow();

        // Initial camera centring (before loop starts; no deltaTime/input yet).
        camera.centreOn(player.getPosition(), projection);

        // Phase 6a/6b: start background music and seed audio tracking state.
        prevCol   = player.getPosition().getCol();
        prevRow   = player.getPosition().getRow();
        prevHp    = player.getHp();
        sound.loopMusic(SoundId.MUSIC_THEME);

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
        if (handleDialogInput()) {
            return;
        }
        prevFollowerActive = follower.isActive();
        handleMouseClick();
        // Any manual WASD press cancels the auto-walk.
        if (input.isDown(KeyEvent.VK_W) || input.isDown(KeyEvent.VK_A)
                || input.isDown(KeyEvent.VK_S) || input.isDown(KeyEvent.VK_D)) {
            follower.stop();
        }
        player.update(deltaTime);
        follower.update(deltaTime, player, map);
        camera.update(deltaTime, input, player.getPosition(), map, projection);
        updateAnimation(deltaTime);
        handleAudio(deltaTime);
        handleDebugPathfinding();
        handleDialogTriggers();
        handleSaveShortcut();
        handleRuntimeToggles();
    }

    /**
     * Phase 6d: while a dialog is open, all gameplay input is suspended and
     * only SPACE / ENTER (edge-triggered) advance the dialog. Returns true
     * if the dialog ate the tick and the engine should skip its normal
     * update step.
     */
    private boolean handleDialogInput() {
        if (!dialogs.isActive()) {
            prevDialogAdvanceKey = input.isDown(KeyEvent.VK_SPACE)
                                || input.isDown(KeyEvent.VK_ENTER);
            return false;
        }
        final boolean advanceDown = input.isDown(KeyEvent.VK_SPACE)
                                 || input.isDown(KeyEvent.VK_ENTER);
        if (advanceDown && !prevDialogAdvanceKey) {
            sound.play(SoundId.CLICK);
            dialogs.advance();
        }
        prevDialogAdvanceKey = advanceDown;
        // Drain the mouse so clicks during a dialog do not queue a path.
        mouse.consume();
        return true;
    }

    /**
     * Phase 6d: opens dialogs from in-game events. Currently:
     *  - {@code E} edge-triggered → debug sample dialog.
     *  - Player just died → death dialog.
     */
    private void handleDialogTriggers() {
        final boolean openDown = input.isDown(KeyEvent.VK_E);
        if (openDown && !prevDialogOpenKey && !dialogs.isActive()) {
            dialogs.show(Dialog.of("System",
                    "Welcome to the Diametric Game Engine.",
                    "Use WASD to walk and the mouse to set a destination.",
                    "Press E any time to reopen this message."));
            sound.play(SoundId.CLICK);
        }
        prevDialogOpenKey = openDown;

        if (prevAlive && !player.isAlive() && !dialogs.isActive()) {
            dialogs.show(Dialog.of("You died",
                    "The lava was hotter than expected.",
                    "Press SPACE to dismiss."));
        }
    }

    /**
     * Phase 7a: edge-triggered F5 dumps the current map to
     * {@code saves/map_YYYYMMDD-HHMMSS.json} so it can be reloaded later.
     */
    private void handleSaveShortcut() {
        final boolean down = input.isDown(KeyEvent.VK_F5);
        if (down && !prevSaveKey) {
            final String stamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            final Path target = SAVE_DIR.resolve("map_" + stamp + ".json");
            try {
                MapWriter.saveToFile(map, target);
                System.out.println("[GameEngine] Map saved to " + target.toAbsolutePath());
                sound.play(SoundId.ARRIVAL);
            } catch (IOException ex) {
                System.err.println("[GameEngine] Failed to save map: " + ex.getMessage());
                sound.play(SoundId.CLICK_INVALID);
            }
        }
        prevSaveKey = down;
    }

    /**
     * Phase 7d: runtime toggles.
     * <ul>
     *   <li>{@code T} → enable/disable tile PNG textures (forces the polygon
     *       placeholder when off).</li>
     *   <li>{@code M} → mute/unmute every SFX and the background music.</li>
     * </ul>
     * Both edges trigger a {@code CLICK} so the user gets audible feedback
     * (only effective for the texture toggle once audio is restored).
     */
    private void handleRuntimeToggles() {
        final boolean texDown = input.isDown(KeyEvent.VK_T);
        if (texDown && !prevTextureToggleKey) {
            final boolean newState = !renderer.getTileRenderer().isTexturesEnabled();
            renderer.getTileRenderer().setTexturesEnabled(newState);
            System.out.println("[GameEngine] Tile textures " + (newState ? "ENABLED" : "DISABLED"));
            sound.play(SoundId.CLICK);
        }
        prevTextureToggleKey = texDown;

        final boolean muteDown = input.isDown(KeyEvent.VK_M);
        if (muteDown && !prevMuteToggleKey) {
            final boolean newMuted = !sound.isMuted();
            sound.setMuted(newMuted);
            System.out.println("[GameEngine] Audio " + (newMuted ? "MUTED" : "UNMUTED"));
            if (!newMuted) sound.play(SoundId.CLICK);
        }
        prevMuteToggleKey = muteDown;
    }

    /**
     * Phase 5d: decides the player's animation state from input + follower
     * and advances the controller clock. Runs after movement so the state
     * reflects what actually happened this tick.
     */
    private void updateAnimation(final double deltaTime) {
        final AnimationController ac = player.getAnimationController();
        if (ac == null) return;
        final boolean walking = follower.isActive()
                || input.isDown(KeyEvent.VK_W) || input.isDown(KeyEvent.VK_A)
                || input.isDown(KeyEvent.VK_S) || input.isDown(KeyEvent.VK_D);
        ac.setState(walking ? AnimationState.WALK : AnimationState.IDLE);
        ac.update(deltaTime);
    }

    /**
     * Phase 6a: derives sound events from the tick's outcome. Footsteps fire
     * every {@link #FOOTSTEP_DISTANCE} tiles of actual movement; HURT fires
     * on HP drop with cooldown to avoid machine-gun ticks; ARRIVAL fires
     * when the auto-walk finishes its route on its own.
     */
    private void handleAudio(final double deltaTime) {
        final float col = player.getPosition().getCol();
        final float row = player.getPosition().getRow();
        final float dist = (float) Math.hypot(col - prevCol, row - prevRow);
        final boolean moving = dist > 0.0005f;
        if (moving) {
            footstepAccum += dist;
            if (footstepAccum >= FOOTSTEP_DISTANCE) {
                sound.play(SoundId.FOOTSTEP);
                footstepAccum = 0f;
            }
        } else {
            footstepAccum = 0f;
        }
        prevCol = col;
        prevRow = row;

        hurtCooldown = Math.max(0f, hurtCooldown - (float) deltaTime);
        final float hp = player.getHp();
        if (hp < prevHp - 0.0005f && hurtCooldown <= 0f && player.isAlive()) {
            sound.play(SoundId.HURT);
            hurtCooldown = HURT_COOLDOWN_SEC;
        }
        if (prevAlive && !player.isAlive()) {
            sound.play(SoundId.DEATH);
        }
        prevHp    = hp;
        prevAlive = player.isAlive();

        // Arrival: follower was active before the tick, finished naturally.
        if (prevFollowerActive && !follower.isActive()) {
            sound.play(SoundId.ARRIVAL);
        }
    }

    /**
     * Phase 4d: left click computes a path from the player's cell to the
     * clicked cell and hands it to the {@link PathFollower}. Right click
     * cancels any auto-walk in progress.
     */
    private void handleMouseClick() {
        final MouseInput.Click click = mouse.consume();
        if (click == null) return;
        if (click.button() == MouseInput.Button.RIGHT) {
            if (follower.isActive()) sound.play(SoundId.CLICK_INVALID);
            follower.stop();
            return;
        }
        if (click.button() != MouseInput.Button.LEFT) return;

        final ScreenPoint worldPx = camera.screenToWorldPixel(click.x(), click.y());
        final int[] picked = projection.pickCell(worldPx.getX(), worldPx.getY(),
                map.getLayer(), MAX_PICK_ELEVATION);
        if (picked == null) {
            System.out.println("[Pathfinder] click outside any tile");
            sound.play(SoundId.CLICK_INVALID);
            return;
        }
        final int tc = picked[0];
        final int tr = picked[1];
        final int fc = Math.round(player.getPosition().getCol());
        final int fr = Math.round(player.getPosition().getRow());
        final List<PathNode> path = pathfinder.findPath(fc, fr, tc, tr);
        if (path.isEmpty()) {
            System.out.println("[Pathfinder] click (" + tc + "," + tr + ") unreachable");
            sound.play(SoundId.CLICK_INVALID);
            follower.stop();
        } else {
            System.out.println("[Pathfinder] click (" + fc + "," + fr + ") -> ("
                + tc + "," + tr + ") = " + path.size() + " steps");
            sound.play(SoundId.CLICK);
            follower.setPath(path);
        }
    }

    /**
     * Phase 4c debug: edge-triggered {@code P} computes a path from the
     * player's current cell to the opposite corner of the map and stores it
     * for the renderer to overlay. A second press from inside that corner
     * clears the overlay.
     */
    private void handleDebugPathfinding() {
        final boolean down = input.isDown(KeyEvent.VK_P);
        if (down && !prevPathDown) {
            pathOverlayActive = !pathOverlayActive;
            if (!pathOverlayActive) {
                debugPath = Collections.emptyList();
            }
        }
        prevPathDown = down;

        if (pathOverlayActive) {
            final int fc = Math.round(player.getPosition().getCol());
            final int fr = Math.round(player.getPosition().getRow());
            final int tc = map.getWidth() / 2;
            final int tr = map.getDepth() / 2;
            debugPath = pathfinder.findPath(fc, fr, tc, tr);
        }
    }

    /** Renders the current frame. */
    public void render() {
        final Graphics2D g = window.getGraphics2D();
        try {
            final List<PathNode> overlay = follower.isActive() ? follower.remaining() : debugPath;
            renderer.render(g, map, player, camera, loop.getCurrentFps(), loop.getCurrentUps(), overlay, dialogs);
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
        smokeCheckResourceIo();
        final String resource = GameConfig.STARTUP_MAP;
        try {
            final TileMap loaded = MapLoader.loadFromClasspath(resource);
            System.out.println("[GameEngine] Loaded map '" + resource + "' ("
                + loaded.getWidth() + "x" + loaded.getDepth() + ").");
            logMapResourceSnapshot(loaded);
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

    /**
     * Round-trips the default Skin / Material / Tile registries (Phase 8e)
     * plus a synthetic {@link Structure} (Phase 9b) through the JSON I/O
     * kernel. Verifies every serialised payload parses back into a payload
     * of the same size; failures are logged but never crash the engine.
     */
    private static void smokeCheckResourceIo() {
        try {
            final int skinsIn     = SkinRegistry.loadDefaults().skins().size();
            final int materialsIn = MaterialRegistry.loadDefaults().materials().size();
            final int tilesIn     = TileDefinitionRegistry.DEFAULT.definitions().size();

            final int skinsOut = SkinSetIO.parse(
                SkinSetIO.toJson(List.copyOf(SkinRegistry.loadDefaults().skins()))).size();
            final int materialsOut = MaterialSetIO.parse(
                MaterialSetIO.toJson(List.copyOf(MaterialRegistry.loadDefaults().materials()))).size();
            final int tilesOut = TileSetIO.parse(
                TileSetIO.toJson(List.copyOf(TileDefinitionRegistry.DEFAULT.definitions()))).size();

            final Structure pillar = new Structure("smoke_pillar", "Smoke Pillar", List.of(
                new TileInstance("stone_block", 0, 0, 0),
                new TileInstance("stone_block", 0, 0, 1),
                new TileInstance("stone_block", 0, 0, 2)));
            final List<Structure> roundTrippedStructures = StructureSetIO.parse(
                StructureSetIO.toJson(List.of(pillar)));
            final int structuresOut = roundTrippedStructures.size();
            final int pillarTiles   = roundTrippedStructures.isEmpty()
                ? 0 : roundTrippedStructures.get(0).tiles().size();

            if (skinsIn == skinsOut
                    && materialsIn == materialsOut
                    && tilesIn == tilesOut
                    && structuresOut == 1
                    && pillarTiles == pillar.tiles().size()) {
                System.out.println("[GameEngine] Phase 8e/9b resource I/O round-trip OK ("
                    + skinsOut + " skins, " + materialsOut + " materials, "
                    + tilesOut + " tile defs, "
                    + structuresOut + " structures with " + pillarTiles + " tiles, "
                    + "formatVersion=" + SkinSetIO.FORMAT_VERSION + ").");
            } else {
                System.err.println("[GameEngine] Resource I/O round-trip count mismatch: "
                    + "skins "      + skinsIn      + "->" + skinsOut      + ", "
                    + "materials "  + materialsIn  + "->" + materialsOut  + ", "
                    + "tiles "      + tilesIn      + "->" + tilesOut      + ", "
                    + "structures " + 1            + "->" + structuresOut + " (" + pillarTiles + " inner tiles).");
            }
        } catch (final RuntimeException ex) {
            System.err.println("[GameEngine] Resource I/O round-trip failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Reports a Phase 9c {@link MapResource} snapshot of the just-loaded
     * runtime map: dimensions and a tile-instance count derived through
     * {@link MapResource#fromTileMap(TileMap)}. Pure observation, never
     * mutates the engine state.
     */
    private static void logMapResourceSnapshot(final TileMap loaded) {
        try {
            final MapResource resource = MapResource.fromTileMap(loaded);
            System.out.println("[GameEngine] Phase 9c MapResource snapshot: "
                + resource.width()  + "x"
                + resource.depth()  + "x"
                + resource.length() + ", "
                + resource.tiles().size()      + " tile instances, "
                + resource.structures().size() + " structure instances.");
        } catch (final RuntimeException ex) {
            System.err.println("[GameEngine] MapResource snapshot failed: " + ex.getMessage());
        }
    }
}
