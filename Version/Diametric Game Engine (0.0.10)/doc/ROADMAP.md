# Roadmap

| Phase | Title                          | Status        |
| ----- | ------------------------------ | ------------- |
| 1     | Foundations (loop + render)    | ✅ Done        |
| 2     | Input & player movement        | ✅ Done        |
| 3     | Voxel tiles, material/shape    | ✅ Done        |
| 4     | Camera, scroll & zoom          | 🔄 In progress |
| 5     | Animations & sprites           | ⏳ Pending     |
| 6     | Audio & UI                     | ⏳ Pending     |
| 7     | Map editor & persistence       | ⏳ Pending     |

## Phase 1 — Foundations
Build a runnable engine that opens a window, runs a decoupled UPS/FPS loop, projects a small demo map in 2:1 diametric and draws a placeholder player.

## Phase 2 — Input & player movement
Keyboard listener, world-space movement vectors, axis remapping for the diametric view.

## Phase 3 — Voxel tiles, material/shape split & movement rules
Replaces the old `TileType` with a clean split: **`TileMaterial`** (gameplay:
color, walkable, speed factor, damage, drowning) + **`TileShape`** (geometry:
14 shapes via 4-bit edge mask N=8 E=4 S=2 W=1) + immutable **`Tile` record**
`(material, shape, elevation)`. `TileLayer` becomes a sparse 3D voxel grid
(`TreeMap<Integer,Tile>` per `(col,row)`), with per-level vertical step
`Z_STEP_PX = 16` (independent of `TILE_HEIGHT = 32`). Movement rules extracted
to `MovementValidator` (cardinal edge equality, diagonal = both intermediate
cardinals legal, walk-on-block rule). Speed/damage/drowning effects extracted
to `TerrainEffects` with diagonal (×0.85) and climb (×0.75) penalties. HUD
shows MAT / SHAPE / CELL. See `phases/PHASE_03.md`.

## Phase 4 — Camera scroll, zoom & A*
Scrollable isometric viewport centered on the player (offset in pixels,
scroll with arrow keys or mouse drag). Zoom via `AffineTransform` scale.
Multi-Z visibility: only render tiles within a configurable Z range of the
player. Then A\* pathfinding over the voxel `TileLayer` (edge cost =
`1 / material.speedFactor`, blocked if `!canMove`) and click-to-move using
the inverted camera transform to convert screen → world coordinates.

Steps in order:
- **4a** Camera offset (FOLLOW / FREE modes, arrow-scroll, edge clamp). ✅ Done — see `phases/PHASE_04.md`.
- **4b** Zoom (`AffineTransform`, UI key binding). ⏳ Pending.
- **4c** A\* on `TileLayer` voxel graph. ⏳ Pending.
- **4d** Click-to-move (screen→world via inverse camera transform). ⏳ Pending.

## Phase 5 — Animations & sprites
Sprite sheets, animation states, frame-time interpolation.

## Phase 6 — Audio & UI
SFX/music, HUD widgets, dialog windows.

## Phase 7 — Map editor & persistence
Tile palette, save/load, custom binary or JSON map format.
