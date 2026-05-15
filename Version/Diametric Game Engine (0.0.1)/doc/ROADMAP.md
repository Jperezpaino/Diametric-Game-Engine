# Roadmap

| Phase | Title                          | Status        |
| ----- | ------------------------------ | ------------- |
| 1     | Foundations (loop + render)    | 🔄 In progress |
| 2     | Input & player movement        | ⏳ Pending     |
| 3     | Multi-layer maps & elevation   | ⏳ Pending     |
| 4     | Collision & pathfinding        | ⏳ Pending     |
| 5     | Animations & sprites           | ⏳ Pending     |
| 6     | Audio & UI                     | ⏳ Pending     |
| 7     | Map editor & persistence       | ⏳ Pending     |

## Phase 1 — Foundations
Build a runnable engine that opens a window, runs a decoupled UPS/FPS loop, projects a small demo map in 2:1 diametric and draws a placeholder player.

## Phase 2 — Input & player movement
Keyboard listener, world-space movement vectors, axis remapping for the diametric view.

## Phase 3 — Multi-layer maps & elevation
Add floor / object / overlay layers and `z` elevation. Integrate elevation into the painter's algorithm.

## Phase 4 — Collision & pathfinding
Per-tile walkability, A* pathfinding, click-to-move.

## Phase 5 — Animations & sprites
Sprite sheets, animation states, frame-time interpolation.

## Phase 6 — Audio & UI
SFX/music, HUD widgets, dialog windows.

## Phase 7 — Map editor & persistence
Tile palette, save/load, custom binary or JSON map format.
