# Roadmap

| Phase | Title                          | Status        |
| ----- | ------------------------------ | ------------- |
| 1     | Foundations (loop + render)    | ✅ Done        |
| 2     | Input & player movement        | ✅ Done        |
| 3     | Voxel tiles, material/shape    | ✅ Done        |
| 4     | Camera, scroll & zoom          | ✅ Done        |
| 5     | Animations & sprites           | ✅ Done        |
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
- **4b** Zoom (`AffineTransform`, UI key binding). ✅ Done — see `phases/PHASE_04.md`.
- **4c** A\* on `TileLayer` voxel graph. ✅ Done — see `phases/PHASE_04.md`.
- **4d** Click-to-move (screen→world via inverse camera transform). ✅ Done — see `phases/PHASE_04.md`.

## Phase 5 — Animations & sprites
Frame-based animations driven by `(AnimationState, Direction8)`. A
`SpriteSheet` exposes equally-sized frames addressed by (col, row); an
`Animation` plays a looped column sequence at a fixed frame duration; an
`AnimationController` per entity maps state×direction to animations, ticks
elapsed time and returns the current frame. `EntityRenderer` blits the
current frame anchored by its bottom-centre on the entity's world position,
falling back to the diamond marker when no controller is bound.

Steps in order:
- **5a** SpriteSheet + Animation data structures. ✅ Done — see `phases/PHASE_05.md`.
- **5b** AnimationState + AnimationController (state×direction). ✅ Done — see `phases/PHASE_05.md`.
- **5c** PlaceholderSprite (programmatic 8-dir sheet) + EntityRenderer wired to sprites. ✅ Done — see `phases/PHASE_05.md`.
- **5d** Entity controller field + GameEngine state/tick wiring. ✅ Done — see `phases/PHASE_05.md`.

## Phase 6 — Audio & UI
SFX/music, HUD widgets, dialog windows.

- **6a** SFX engine + sound events (footstep, click, arrival, hurt, death). ✅ Done — `audio` package with `SoundManager` + `PlaceholderAudio` fallback, wired into `GameEngine`.
- **6b** Background music loop. ✅ Done — `MUSIC_THEME` arpeggio looped at boot; real WAV at `audio/music/theme.wav` takes precedence.
- **6c** HUD enhancements (HP bar + active path destination). ✅ Done — `GameRenderer.drawHpBar()` + PATH line shows destination cell.
- **6d** Dialog windows. ✅ Done — `ui.dialog` package with `Dialog` + `DialogManager`, paginated box at bottom; E opens debug dialog, death triggers an automatic one.

## Phase 7 — Map editor & persistence
Tile palette, save/load, custom binary or JSON map format.
