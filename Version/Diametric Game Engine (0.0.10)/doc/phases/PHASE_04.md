# Phase 4 — Camera, scroll & zoom

## Status

- **4a** Camera offset (FOLLOW / FREE, edge clamp) — ✅ Done
- **4b** Zoom (`AffineTransform`) — ⏳ Pending
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

After every update (in both modes), `clampToMap` keeps the visible viewport
within the map's screen-space bounding box, with a `CAMERA_EDGE_MARGIN` (px)
slack so the very edge tiles are not glued against the screen border.

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

## 4b / 4c / 4d

Pending. See `ROADMAP.md`.
