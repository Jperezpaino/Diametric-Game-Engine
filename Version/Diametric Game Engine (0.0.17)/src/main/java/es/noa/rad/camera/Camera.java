package es.noa.rad.camera;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import es.noa.rad.config.GameConfig;
import es.noa.rad.input.InputState;
import es.noa.rad.map.TileMap;
import es.noa.rad.projection.IsoProjection;
import es.noa.rad.projection.ScreenPoint;
import es.noa.rad.projection.WorldPoint;

/**
 * Viewport offset that translates world drawing into screen space.
 *
 * <p>The camera stores a translation in pixels that is applied to the
 * {@link Graphics2D} before any world drawing takes place.</p>
 *
 * <h3>Modes</h3>
 * <ul>
 *   <li>{@link Mode#FOLLOW} (default) — recentres on the target every frame.</li>
 *   <li>{@link Mode#FREE} — offset is moved manually via the arrow keys at
 *       {@link GameConfig#CAMERA_SCROLL_SPEED} px/s; the target is ignored
 *       until the user switches back.</li>
 * </ul>
 *
 * <h3>Bindings</h3>
 * <ul>
 *   <li>Arrow keys — scroll (auto-switches to {@link Mode#FREE}).</li>
 *   <li>{@code F} — toggle {@link Mode#FOLLOW} / {@link Mode#FREE}.</li>
 *   <li>{@code HOME} — recentre on the target (and switch back to FOLLOW).</li>
 *   <li>{@code +} / {@code =} — zoom in (next discrete level).</li>
 *   <li>{@code -} — zoom out (previous discrete level).</li>
 *   <li>{@code 0} — reset zoom to {@link GameConfig#CAMERA_ZOOM_DEFAULT_INDEX}.</li>
 * </ul>
 *
 * <p>In any mode the offset is clamped so the visible viewport stays within
 * the map's screen-space bounding box, with a {@link GameConfig#CAMERA_EDGE_MARGIN}
 * pixel margin of slack. If the map is smaller than the screen (taking the
 * current zoom into account) it is centred.</p>
 *
 * @since Phase 4a (zoom added in Phase 4b)
 */
public final class Camera {

    /** Camera behaviour. */
    public enum Mode { FOLLOW, FREE }

    private double offsetX;
    private double offsetY;
    private Mode   mode = Mode.FOLLOW;
    private int    zoomIndex = GameConfig.CAMERA_ZOOM_DEFAULT_INDEX;

    // Edge-triggered toggle state.
    private boolean prevToggleDown;
    private boolean prevHomeDown;
    private boolean prevZoomInDown;
    private boolean prevZoomOutDown;
    private boolean prevZoomResetDown;

    /**
     * Advances the camera by one tick.
     *
     * @param deltaTime  elapsed seconds since the previous update
     * @param input      keyboard state
     * @param target     world position to follow
     * @param map        loaded map (used for bounds clamping)
     * @param projection isometric projection
     */
    public void update(final double deltaTime,
                       final InputState input,
                       final WorldPoint target,
                       final TileMap map,
                       final IsoProjection projection) {

        // --- Edge-triggered toggles --------------------------------------
        final boolean toggleDown = input.isDown(KeyEvent.VK_F);
        if (toggleDown && !prevToggleDown) {
            mode = (mode == Mode.FOLLOW) ? Mode.FREE : Mode.FOLLOW;
        }
        prevToggleDown = toggleDown;

        final boolean homeDown = input.isDown(KeyEvent.VK_HOME);
        if (homeDown && !prevHomeDown) {
            mode = Mode.FOLLOW;
            centreOn(target, projection);
        }
        prevHomeDown = homeDown;

        // --- Edge-triggered zoom ----------------------------------------
        // Any zoom change snaps the camera back to FOLLOW and recentres on
        // the target. If the player then wants to look elsewhere they just
        // tap an arrow, which auto-switches to FREE as usual.
        // VK_PLUS (US layout) and VK_EQUALS (same physical key without Shift)
        // both count as zoom-in; VK_ADD covers the numeric keypad.
        final boolean zoomInDown =
                input.isDown(KeyEvent.VK_PLUS)
             || input.isDown(KeyEvent.VK_EQUALS)
             || input.isDown(KeyEvent.VK_ADD);
        if (zoomInDown && !prevZoomInDown && setZoomIndex(zoomIndex + 1)) {
            mode = Mode.FOLLOW;
            centreOn(target, projection);
        }
        prevZoomInDown = zoomInDown;

        final boolean zoomOutDown =
                input.isDown(KeyEvent.VK_MINUS)
             || input.isDown(KeyEvent.VK_SUBTRACT);
        if (zoomOutDown && !prevZoomOutDown && setZoomIndex(zoomIndex - 1)) {
            mode = Mode.FOLLOW;
            centreOn(target, projection);
        }
        prevZoomOutDown = zoomOutDown;

        final boolean zoomResetDown =
                input.isDown(KeyEvent.VK_0)
             || input.isDown(KeyEvent.VK_NUMPAD0);
        if (zoomResetDown && !prevZoomResetDown) {
            setZoomIndex(GameConfig.CAMERA_ZOOM_DEFAULT_INDEX);
            mode = Mode.FOLLOW;
            centreOn(target, projection);
        }
        prevZoomResetDown = zoomResetDown;

        // --- Free-scroll input -------------------------------------------
        double dx = 0.0, dy = 0.0;
        if (input.isDown(KeyEvent.VK_LEFT))  dx += 1.0;
        if (input.isDown(KeyEvent.VK_RIGHT)) dx -= 1.0;
        if (input.isDown(KeyEvent.VK_UP))    dy += 1.0;
        if (input.isDown(KeyEvent.VK_DOWN))  dy -= 1.0;
        final boolean scrolling = (dx != 0.0 || dy != 0.0);

        if (scrolling) {
            // First arrow press while following snaps us into FREE.
            if (mode == Mode.FOLLOW) {
                mode = Mode.FREE;
            }
            // Divide by zoom so scrolling feels the same in world units
            // regardless of the current zoom level.
            final double speed = (GameConfig.CAMERA_SCROLL_SPEED * deltaTime) / getZoom();
            offsetX += dx * speed;
            offsetY += dy * speed;
        }

        // --- Follow mode keeps target centred -----------------------------
        if (mode == Mode.FOLLOW) {
            centreOn(target, projection);
            // In FOLLOW the player must always be on screen-centre, even
            // near a map edge at high zoom (where the clamp would otherwise
            // push the offset away from the target). The cost is showing a
            // bit of background past the map border, which is acceptable.
            return;
        }

        // --- FREE mode: clamp to map bounds -------------------------------
        clampToMap(map, projection);
    }

    /**
     * Centres the viewport on the given world position immediately.
     * Useful before the loop starts (no {@code deltaTime} / input yet).
     */
    public void centreOn(final WorldPoint target, final IsoProjection projection) {
        final ScreenPoint sp = projection.worldToScreen(target);
        offsetX = (GameConfig.SCREEN_WIDTH  / 2.0) - sp.getX();
        offsetY = (GameConfig.SCREEN_HEIGHT / 2.0) - sp.getY();
    }

    /**
     * Clamps the offset so the screen viewport stays within the map's
     * screen-space bounding box (computed by projecting the four ground
     * corners), with {@link GameConfig#CAMERA_EDGE_MARGIN} pixels of slack.
     *
     * <p>The visible viewport spans {@code SCREEN_W / zoom} world-screen
     * pixels horizontally and {@code SCREEN_H / zoom} vertically, so the
     * legal {@code offset} range depends on the current zoom level.</p>
     */
    private void clampToMap(final TileMap map, final IsoProjection projection) {
        final int w = map.getWidth();
        final int d = map.getDepth();
        final ScreenPoint s00 = projection.worldToScreen(new WorldPoint(0, 0, 0));
        final ScreenPoint sW0 = projection.worldToScreen(new WorldPoint(w, 0, 0));
        final ScreenPoint s0D = projection.worldToScreen(new WorldPoint(0, d, 0));
        final ScreenPoint sWD = projection.worldToScreen(new WorldPoint(w, d, 0));

        final double minX = min4(s00.getX(), sW0.getX(), s0D.getX(), sWD.getX());
        final double maxX = max4(s00.getX(), sW0.getX(), s0D.getX(), sWD.getX());
        final double minY = min4(s00.getY(), sW0.getY(), s0D.getY(), sWD.getY());
        final double maxY = max4(s00.getY(), sW0.getY(), s0D.getY(), sWD.getY());

        final double margin = GameConfig.CAMERA_EDGE_MARGIN;
        final double sw     = GameConfig.SCREEN_WIDTH;
        final double sh     = GameConfig.SCREEN_HEIGHT;
        final double zoom   = getZoom();
        final double viewW  = sw / zoom;   // visible width  in world-screen px
        final double viewH  = sh / zoom;   // visible height in world-screen px
        final double mapW   = maxX - minX;
        final double mapH   = maxY - minY;

        // Viewport horizontal bounds in world-screen coords (derived from
        // apply()'s composed transform): left = S*(1 - 1/zoom) - offsetX,
        // right = S*(1 + 1/zoom) - offsetX, with S = SCREEN_W / 2.
        final double sX = sw / 2.0;
        if (mapW + 2 * margin <= viewW) {
            // Map narrower than the viewport: centre it.
            offsetX = sX - ((minX + maxX) / 2.0);
        } else {
            final double minOff = sX * (1.0 - 1.0 / zoom) - minX - margin; // left  <= minX + margin
            final double maxOff = sX * (1.0 + 1.0 / zoom) - maxX + margin; // right >= maxX - margin
            if (offsetX < minOff) offsetX = minOff;
            if (offsetX > maxOff) offsetX = maxOff;
        }

        final double sY = sh / 2.0;
        if (mapH + 2 * margin <= viewH) {
            offsetY = sY - ((minY + maxY) / 2.0);
        } else {
            final double minOff = sY * (1.0 - 1.0 / zoom) - minY - margin;
            final double maxOff = sY * (1.0 + 1.0 / zoom) - maxY + margin;
            if (offsetY < minOff) offsetY = minOff;
            if (offsetY > maxOff) offsetY = maxOff;
        }
    }

    /**
     * Applies the current offset and zoom to {@code g} so subsequent world
     * drawing is shifted into view. The transform scales around the centre
     * of the screen so zooming feels stable. The whole composed transform
     * is left active on {@code g}; callers are expected to restore the
     * previous transform with {@code g.setTransform(saved)} before drawing
     * the HUD.
     */
    public void apply(final Graphics2D g) {
        final double zoom = getZoom();
        final double sX   = GameConfig.SCREEN_WIDTH  / 2.0;
        final double sY   = GameConfig.SCREEN_HEIGHT / 2.0;
        // Snap offset so that the final screen-space translation lands on
        // whole pixels (avoids sub-pixel jitter, especially at zoom = 1).
        final double snapX = Math.round(offsetX * zoom) / zoom;
        final double snapY = Math.round(offsetY * zoom) / zoom;
        // Compose: translate-to-centre → scale → translate-by-offset-from-centre.
        g.translate(sX, sY);
        g.scale(zoom, zoom);
        g.translate(-sX + snapX, -sY + snapY);
    }

    /**
     * Sets {@code zoomIndex} clamped to the valid range.
     *
     * @return {@code true} if the index actually changed.
     */
    private boolean setZoomIndex(final int idx) {
        final int n = GameConfig.CAMERA_ZOOM_LEVELS.length;
        final int clamped = Math.max(0, Math.min(n - 1, idx));
        if (clamped == zoomIndex) {
            return false;
        }
        zoomIndex = clamped;
        return true;
    }

    public Mode   getMode()      { return mode;      }
    public double getOffsetX()   { return offsetX;   }
    public double getOffsetY()   { return offsetY;   }
    public int    getZoomIndex() { return zoomIndex; }
    public double getZoom()      { return GameConfig.CAMERA_ZOOM_LEVELS[zoomIndex]; }

    private static double min4(final double a, final double b, final double c, final double d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }
    private static double max4(final double a, final double b, final double c, final double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }
}
