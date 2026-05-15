# Roadmap

| Phase | Title                          | Status        |
| ----- | ------------------------------ | ------------- |
| 1     | Foundations (loop + render)    | ✅ Done        |
| 2     | Input & player movement        | ✅ Done        |
| 3     | Multi-layer maps & elevation   | ✅ Done        |
| 4     | Collision & pathfinding        | 🔄 In progress |
| 5     | Animations & sprites           | ⏳ Pending     |
| 6     | Audio & UI                     | ⏳ Pending     |
| 7     | Map editor & persistence       | ⏳ Pending     |

## Phase 1 — Foundations
Build a runnable engine that opens a window, runs a decoupled UPS/FPS loop, projects a small demo map in 2:1 diametric and draws a placeholder player.

## Phase 2 — Input & player movement
Keyboard listener, world-space movement vectors, axis remapping for the diametric view.

## Phase 3 — Multi-layer maps & elevation
Visual + object tile layers and Z elevation modelled per-corner (4 corners
per diamond, integer heights). 8 ramp types (4 edge ramps + 4 single-corner
ramps) plus flat plateaus (`ELEVATED` / `CLIFF`). Collision based on
**shared-edge corner matching** between source and target tile, so vertical
steps are blocked and the player must use a ramp. Z is interpolated
bilinearly inside each tile for a gradual climb. Side faces rendered with
two darken levels for depth. See `phases/PHASE_03.md`.

## Phase 4 — Collision & pathfinding
Per-tile walkability, A* pathfinding, click-to-move.

## Phase 5 — Animations & sprites
Sprite sheets, animation states, frame-time interpolation.

## Phase 6 — Audio & UI
SFX/music, HUD widgets, dialog windows.

## Phase 7 — Map editor & persistence
Tile palette, save/load, custom binary or JSON map format.
