package es.noa.rad.config;

/**
 * Global configuration constants for the Diametric Game Engine.
 *
 * <p>This class centralises every fixed parameter of the engine so that no
 * magic numbers appear scattered across the codebase. All values are defined
 * as {@code public static final} constants and the class cannot be
 * instantiated.</p>
 *
 * <p>Phase 1 scope: window resolution, tile dimensions (2:1 diametric ratio)
 * and target update / render rates.</p>
 *
 * @since Phase 1
 */
public final class GameConfig {

    /** Window width in pixels. */
    public static final int SCREEN_WIDTH = 800;

    /** Window height in pixels. */
    public static final int SCREEN_HEIGHT = 600;

    /** Tile width in pixels (must be 2 * TILE_HEIGHT for 2:1 projection). */
    public static final int TILE_WIDTH = 64;

    /** Tile height in pixels (visual half-height of the diamond). */
    public static final int TILE_HEIGHT = 32;

    /**
     * Vertical pixel step per Z (elevation) level. Independent from
     * {@link #TILE_HEIGHT}: a tile elevated one level rises by this many
     * screen pixels. Per the reference assets in {@code doc/Info/} this is
     * exactly 16 px (half of {@code TILE_HEIGHT}).
     *
     * @since Phase 3
     */
    public static final int Z_STEP_PX = 16;

    /** Target frames per second (render rate). */
    public static final int TARGET_FPS = 60;

    /** Target updates per second (logic rate). */
    public static final int TARGET_UPS = 60;

    /** Window title. */
    public static final String WINDOW_TITLE = "Diametric Game Engine";

    /**
     * Classpath resource of the map loaded at startup. Change this value
     * (and recompile) to switch between the test maps stored under
     * {@code src/main/resources/map/}. If the resource is missing or
     * malformed the engine falls back to the built-in demo map.
     *
     * @since Phase 3 debt-close
     */
    public static final String STARTUP_MAP = "map/map_8.json";

    /**
     * Scroll speed of the free camera in screen pixels per second.
     *
     * @since Phase 4a
     */
    public static final int CAMERA_SCROLL_SPEED = 400;

    /**
     * Extra screen-space margin (px) the camera is allowed to show beyond
     * the map's bounding box on each side. Prevents the viewport from
     * showing nothing but background when scrolled to the edge.
     *
     * @since Phase 4a
     */
    public static final int CAMERA_EDGE_MARGIN = 64;

    /**
     * Discrete zoom levels available to the camera, in ascending order.
     * Integer levels keep pixel art crisp; {@code 0.5} provides a wider
     * tactical view.  Index into this array via {@link #CAMERA_ZOOM_DEFAULT_INDEX}.
     *
     * @since Phase 4b
     */
    public static final double[] CAMERA_ZOOM_LEVELS = { 0.5, 1.0, 2.0, 3.0 };

    /** Index in {@link #CAMERA_ZOOM_LEVELS} used at boot and on reset. */
    public static final int CAMERA_ZOOM_DEFAULT_INDEX = 1; // 1.0x

    private GameConfig() {
        // Utility class, no instances allowed.
    }
}
