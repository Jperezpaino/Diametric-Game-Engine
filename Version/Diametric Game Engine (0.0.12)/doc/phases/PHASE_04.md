# Phase 4 — Camera, scroll & zoom

## Status

- **4a** Camera offset (FOLLOW / FREE, edge clamp) — ✅ Done
- **4b** Zoom (`AffineTransform`) — ✅ Done
- **4c** A\* on `TileLayer` voxel graph — ⏳ Pending
- **4d** Click-to-move (screen → world via inverse camera transform) — ⏳ Pending

---

## 4a — Camera offset

The `Camera` (in `es.noa.rad.camera.Camera`) is a screen-space translation
applied to `Graphics2D` before any world drawing. The renderer asks the camera
for `apply(g)` once per frame, then draws the world in absolute screen
coordinates as if `(0, 0)` were the top-left of the map plane; the camera takes
care of shifting everything into view.

### Modes

| Mode     | Behaviour                                                                 |
| -------- | ------------------------------------------------------------------------- |
| `FOLLOW` | Recentres on the target (`player.getPosition()`) **every frame**.         |
| `FREE`   | Offset moves manually with the arrow keys; target is ignored.             |

The default at boot is `FOLLOW`. The initial centring is done **once** before
the game loop starts via `Camera.centreOn(target, projection)`, which does not
need `deltaTime` nor an `InputState`.

### Bindings

| Key         | Action                                                                |
| ----------- | --------------------------------------------------------------------- |
| `←` `→` `↑` `↓` | Scroll the viewport at `CAMERA_SCROLL_SPEED` px/s; auto-switches to `FREE`. |
| `F`         | Toggle `FOLLOW` ↔ `FREE` (edge-triggered).                            |
| `HOME`      | Recentre on target **and** switch back to `FOLLOW` (edge-triggered).  |

Arrow keys were removed from `Player` movement (they were duplicating WASD)
so they could be repurposed for the camera without conflict. WASD remains the
movement binding.

Edge-triggered toggles use a `prevToggleDown` / `prevHomeDown` flag stored on
the camera so holding `F` does not flip the mode every tick.

### Map-bounds clamping

After every update in **`FREE` mode**, `clampToMap` keeps the visible
viewport within the map's screen-space bounding box, with a
`CAMERA_EDGE_MARGIN` (px) slack so the very edge tiles are not glued
against the screen border.

In **`FOLLOW` mode** the clamp is **skipped**: the player must always be at
the screen centre, even near a map edge at high zoom (where the clamp
would otherwise push the offset away from the target). The cost is showing
a small strip of background past the map border, which is the natural
trade-off in any tile-based RPG/RTS following camera.

Algorithm:

1. Project the four ground-plane map corners
   `(0,0,0)`, `(W,0,0)`, `(0,D,0)`, `(W,D,0)` to screen via `IsoProjection`
   (with no camera offset applied — these are absolute screen coords).
2. Compute the axis-aligned bounding box `[minX, maxX] × [minY, maxY]`.
3. For each axis, derive the legal range of `offset`:
   - viewport-left in absolute coords  = `-offset`
   - viewport-right in absolute coords = `-offset + SCREEN_W`
   - So `viewport-right ≥ maxX − margin` gives `offset ≥ SCREEN_W − maxX − margin`
   - And `viewport-left ≤ minX + margin` gives `offset ≤ -minX + margin`.
4. If the map is smaller than the screen along that axis
   (`mapDim + 2·margin ≤ screenDim`), centre it instead of clamping
   (so the map sits in the middle rather than glued to one corner).

This works for any isometric projection because we measure the bbox in the
**rendered** screen space, not in world units.

### Configuration (`GameConfig`)

| Constant                | Default | Meaning                                          |
| ----------------------- | ------- | ------------------------------------------------ |
| `CAMERA_SCROLL_SPEED`   | `400`   | Free-camera scroll speed in pixels per second.   |
| `CAMERA_EDGE_MARGIN`    | `64`    | Slack past the map bbox in pixels.               |

### HUD line 3

`GameRenderer` now draws a third HUD line at `y = 56`:

```
CAM: FOLLOW  off:[123, -45]
CAM: FREE    off:[ 80, -10]
```

It shows the current mode and the rounded screen-space offset.

### Files touched

- `config/GameConfig.java` — added `CAMERA_SCROLL_SPEED`, `CAMERA_EDGE_MARGIN`.
- `camera/Camera.java` — rewritten: `Mode` enum, 5-arg `update`, `centreOn`,
  `clampToMap`, mode/offset getters.
- `core/GameEngine.java` — initial `centreOn` before the loop; per-tick
  `camera.update(deltaTime, input, target, map, projection)`.
- `entity/Player.java` — removed arrow-key bindings so arrows are free for
  the camera (WASD still moves the player).
- `render/GameRenderer.java` — added HUD line 3 with camera mode / offset.

### Manual test plan

1. Run `mvn -q exec:java`.
2. WASD: player moves; camera follows (FOLLOW visible in HUD).
3. Tap an arrow key: HUD switches to `FREE`, view scrolls.
4. Try to scroll past a map edge: clamps with ~64 px margin.
5. Press `F`: toggles FREE ↔ FOLLOW; in FOLLOW the view snaps back to player.
6. Scroll away in FREE, press `HOME`: recentres on player and switches to FOLLOW.
7. On a small map (smaller than the screen) the map is centred and arrow
   keys produce no visible motion.

---

## 4b — Discrete zoom

Discrete zoom levels keep pixel art crisp and the math simple. The available
levels are configured in `GameConfig.CAMERA_ZOOM_LEVELS` (default
`{0.5, 1.0, 2.0, 3.0}`) and the default index is
`CAMERA_ZOOM_DEFAULT_INDEX` (`1`, i.e. `1.0x`).

### Transform

`Camera.apply(g)` now composes a translate–scale–translate around the screen
centre so zoom feels stable instead of dragging content towards `(0,0)`:

```
g.translate(SCREEN_W/2, SCREEN_H/2);
g.scale(zoom, zoom);
g.translate(-SCREEN_W/2 + offsetX, -SCREEN_H/2 + offsetY);
```

The `offsetX/Y` still live in **pre-zoom (world-screen) pixels**, so all
existing offset semantics carry over from 4a. For pixel-art crispness the
offset is snapped so the final screen-space translation lands on whole
pixels: `round(offset * zoom) / zoom`.

`GameRenderer` already draws the world inside a `g.setTransform(saved)`
sandwich and renders the HUD after restoring the original transform, so
the HUD remains immune to zoom.

### Bindings (edge-triggered)

| Key                        | Action                                         |
| -------------------------- | ---------------------------------------------- |
| `+` / `=` / numpad `+`     | Zoom in (next discrete level).                 |
| `-` / numpad `-`           | Zoom out (previous discrete level).            |
| `0` / numpad `0`           | Reset to `CAMERA_ZOOM_DEFAULT_INDEX` (`1.0x`). |

The `=` key shares the physical `+` key on US layouts without needing
Shift, so both are bound.

Any **effective** zoom change (i.e. one that actually moves the index)
snaps the camera back to `FOLLOW` and recentres on the target. Reset (`0`)
also forces `FOLLOW` + recentre even if the zoom was already at default,
so it doubles as a "look at me" shortcut. If the user then wants to look
elsewhere they just tap an arrow key — that already auto-switches to FREE.
A zoom-in attempt at the maximum level (or zoom-out at the minimum) is a
no-op and does **not** change the mode.

### Scroll speed

To keep perceived motion constant across zoom levels, free-camera scroll
is divided by the zoom factor:

```
speed = CAMERA_SCROLL_SPEED * dt / zoom
```

At `3x` zoom each arrow press moves the offset 3× slower in offset-space,
which translates to the same on-screen pixels per second.

### Clamp at non-unit zoom

The visible viewport in pre-zoom (offset) coordinates spans
`SCREEN_W / zoom` × `SCREEN_H / zoom` pixels. The clamp logic projects the
four ground corners of the map, derives `[minX, maxX] × [minY, maxY]`, and
solves for the legal `offset` range using the composed transform:

```
viewport-left  (abs) = S * (1 - 1/zoom) - offset
viewport-right (abs) = S * (1 + 1/zoom) - offset
   with S = SCREEN_W / 2
```

- `offset ≤ S(1 + 1/zoom) - maxX + margin`  (right ≥ maxX − margin)
- `offset ≥ S(1 - 1/zoom) - minX - margin`  (left  ≤ minX + margin)

If the map bbox plus margins fits in the current viewport
(`mapDim + 2·margin ≤ SCREEN_DIM / zoom`), the map is centred instead.

### HUD line 3

The third HUD line now also shows the zoom:

```
CAM: FOLLOW  off:[123, -45]  zoom:1x
CAM: FREE    off:[ 80, -10]  zoom:2x
```

### Files touched

- `config/GameConfig.java` — added `CAMERA_ZOOM_LEVELS` and
  `CAMERA_ZOOM_DEFAULT_INDEX`.
- `camera/Camera.java` — discrete `zoomIndex`, edge-triggered `+`/`-`/`0`
  bindings, composed transform in `apply`, zoom-aware `clampToMap`, scroll
  speed divided by zoom, `getZoom()` / `getZoomIndex()` getters.
- `render/GameRenderer.java` — HUD line 3 now shows the zoom factor.

### Manual test plan

1. Run `mvn -q exec:java`. At boot zoom is `1.0x` (HUD line 3 confirms).
2. Press `+` twice → zoom goes `1 → 2 → 3`. World magnifies around the
   screen centre; the player stays centred in FOLLOW.
3. Press `-` repeatedly → zoom returns through `2, 1, 0.5`. Further `-`
   is a no-op (already at min).
4. Press `0` → instantly back to `1.0x`.
5. With zoom `3x` and FREE mode, hold an arrow: scrolling feels the same
   per second as at `1x`.
6. At zoom `0.5x` on a small map the whole map fits in the viewport →
   `clampToMap` centres it and arrow keys produce no motion.
7. At zoom `3x` scrolling stops earlier (less map can be hidden past the
   margin) — clamp limits are tighter.

---

## 4c / 4d

Pending. See `ROADMAP.md`.
