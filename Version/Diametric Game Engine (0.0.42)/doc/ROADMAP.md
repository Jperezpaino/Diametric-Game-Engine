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

## Phase 7 — Asset pipeline
Replace programmatic placeholder rendering with real PNG assets (tiles + entity sprites).

- **7a** `MapWriter` JSON serializer + F5 save shortcut. ✅ Done — writes to `saves/map_TIMESTAMP.json`, round-trips through `MapLoader`.
- **7b** Tile image pipeline — `TileRenderer` loads PNG per `(TileMaterial, TileShape)`; fallback to current polygon if asset missing. ✅ Done — `TileSpriteRegistry` probes `tiles/<material>_<shape>.png` at startup, `TileRenderer` blits sprite (bottom-center on south apex) or falls back to polygon.
- **7c** Entity sprite pipeline — `EntityRenderer` loads real PNG atlas; fallback to `PlaceholderSprite` if asset missing. ✅ Done — `EntitySprites.tryLoadController("<name>")` reads `entities/<name>.png` (96×384, 3×8 grid of 32×48, magenta keying) and returns a fully bound `AnimationController`; `GameEngine` falls back to `PlaceholderSprite.createController()` when null. Shared loader extracted to `AssetImages`.
- **7d** Runtime toggles — `T` enables/disables tile PNG textures (forces polygon placeholder); `M` mutes/unmutes every SFX + background music. ✅ Done — edge-triggered in `GameEngine.handleRuntimeToggles()`, state lives in `TileRenderer.texturesEnabled` and `SoundManager.muted`.

## Phase 8 — Domain model refactor (engine-side)
Align the in-memory model with the [Map System spec](Info/maps/Especificación%20Técnica%20del%20Sistema%20de%20Mapas%20Isométricos%203D.md): split visual from physical, introduce id-based registries, version every resource format. No UI yet — game runtime keeps booting through every step.

- **8a** `Skin` abstraction — extract the visual asset from `TileMaterial` into a `Skin` record (`id`, `name`, `imageRef`); `TileSpriteRegistry` consumes `Skin` ids instead of `(material, shape)`. ✅ Done — `map.Skin` record + `render.SkinRegistry` (single source of truth for image loading via `AssetImages`); `TileSpriteRegistry` is now a thin adapter that maps `(material, shape)` → synthetic skin id `<material>_<shape>` → cached `BufferedImage`. Default skin set seeded for every enum pair, matching the Phase 7b naming convention.
- **8b** `Material` registry — convert `TileMaterial` enum into a registry-backed `Material` record (`id`, `name`, `solid`, `damage`, `speedModifier`, `properties`); preserve every current material as a default entry. ✅ Done — `map.Material` record with open `properties` bag + `boolProperty/floatProperty/stringProperty` helpers; `map.MaterialRegistry` with `register/get/materials` and `loadDefaults()` seeded from the legacy enum. `TileMaterial` gained `solid` flag, `materialId()` and `toMaterial()`; legacy getters preserved so every consumer (`Tile`, `TerrainEffects`, `Pathfinder`, renderers, `MapWriter/Loader`) keeps working unchanged.
- **8c** Shape vocabulary lock-in — keep the engine's richer `TileShape` enum (`FLOOR`, `BLOCK`, 4 `RAMP_*`, 4 `DOUBLE_RAMP_*`, 4 `CONCAVE_*`) as the canonical shape vocabulary; the spec's example set (`FLAT/WALL/SLOPE_*/STAIR_*`) is non-exhaustive so no rename or migration is needed. Surface a `shapeId()` (lowercase enum name) and `displayName()` for editor dropdowns; document the spec equivalence in javadoc. ✅ Done — added `TileShape.shapeId()` + `displayName()` and the equivalence table (`FLAT≈FLOOR`, `WALL≈BLOCK`, `SLOPE_NORTH≈DOUBLE_RAMP_N`, …); `RAMP_*` and `CONCAVE_*` flagged as engine-specific extensions.
- **8d** `TileDefinition` registry — introduce a `TileDefinition` record (`id`, `name`, `skinId`, `materialId`, `shape`) and `TileDefinitionRegistry` as the canonical catalogue of tile recipes. Default registry seeds one entry per `(TileMaterial, TileShape)` pair, mirroring the synthetic ids used by `SkinRegistry`/`MaterialRegistry`. ✅ Done — `map.TileDefinition` + `map.TileDefinitionRegistry` (`register/get/definitions/defaultId/loadDefaults`); `Tile.definitionId()` bridge accessor added so callers can already work in id-space. Engine runtime untouched — the legacy `(material, shape, elevation)` tuple keeps working.
- **8d-bis** Runtime tile-id migration — flip `Tile` to `(definitionId, elevation)` (or equivalent) and resolve `material`/`shape` through `TileDefinitionRegistry` at every consumer (`TerrainEffects`, `Pathfinder`, renderers, `MapWriter/Loader`, `TileLayer/TileMap`). Breaking but contained inside the engine. ✅ Done — `Tile` now stores `(TileDefinition definition, int elevation)`; legacy `material()`/`shape()` accessors forward to the definition (fast `materialId → TileMaterial` index built once at class init), so every consumer keeps working unchanged. Convenience constructor `Tile(TileMaterial, TileShape, int)` resolves through new public constant `TileDefinitionRegistry.DEFAULT`.
- **8e** Versioned resource I/O — `SkinSet` / `MaterialSet` / `TileSet` JSON format with `formatVersion: 1` on every file; shared `map.io` kernel reads/writes them. ✅ Done — new package-private `Json` helpers (strip-comments, scalar field readers, brace-balanced array/object body extraction, JSON-escape) feed three readers/writers: `SkinSetIO` (`skins`), `MaterialSetIO` (`materials`, with bool/number/string properties bag) and `TileSetIO` (`tiles`). Every file rejects unknown `formatVersion`. `MapWriter` now emits `"formatVersion": 1` as the first field; `MapLoader` reads it as optional (defaults to 1) so every existing `map_*.json` keeps working. A boot-time smoke check round-trips the default registries through serialize → parse and logs `[GameEngine] Phase 8e resource I/O round-trip OK (112 skins, 8 materials, 112 tile defs, formatVersion=1)`.

## Phase 9 — Structure system (engine-side)
Add the missing reusable building block: a Structure is a group of tiles at relative XYZ, instanced N times in a map at absolute XYZ.

- **9a** `Structure` + `StructureInstance` model (relative coords with origin at `(0,0,0)`). ✅ Done — added four pure-domain records/classes in `es.noa.rad.map`: `TileInstance(tileId, x, y, z)` (used both inside structures with relative coords and inside the Phase 9c map with absolute coords), `StructureInstance(structureId, x, y, z)`, `Structure(id, name, List<TileInstance> tiles)` (defensive `List.copyOf`), and `StructureRegistry` (insertion-ordered, mirrors `TileDefinitionRegistry` shape; `loadDefaults()` returns an empty registry — structures are user content). Non-breaking: nothing in the engine consumes them yet.
- **9b** `StructureSet` JSON format (`formatVersion: 1`). ⏳ Pending.
- **9c** Map model v2 — supports both `TileInstance` and `StructureInstance`; absolute coords; formatVersion: 1. ⏳ Pending.
- **9d** New `MapLoader` / `MapWriter` — handle the v2 format with a backward-compat shim that auto-migrates the legacy `map_N.json` palette format. ⏳ Pending.

## Phase 10 — Multi-module Maven layout
Restructure the project so the editors ship as separate runnables that depend on the engine, never the other way round.

- **10a** Parent POM + child module `diametric-engine` (current code, minus any editor-only class). ⏳ Pending.
- **10b** Shared `diametric-tools-common` module — Swing utilities, resource browser widgets, JSON kernel glue. ⏳ Pending.
- **10c** Per-tool runnable modules: `tile-builder`, `structure-builder`, `map-builder`. ⏳ Pending.
- **10d** Game runtime keeps depending only on `diametric-engine`; editor classes are unreachable from the game classpath. ⏳ Pending.

## Phase 11 — UI-001 Tile Builder
Tool for managing Skins, Materials and Tile definitions. Ref: [UI-001 - Tile Builder.md](Info/maps/UI-001%20-%20Tile%20Builder.md).

- **11a** Application skeleton — main window (640×480 min), menu bar, toolbar, status bar, welcome screen with concept image. ⏳ Pending.
- **11b** Tile Library panel (left) + Tile Preview (centre) + Properties panel (right). ⏳ Pending.
- **11c** `Skin Manager` dialog — import/list/preview/delete PNG resources. ⏳ Pending.
- **11d** `Material Manager` dialog — CRUD for materials with `name`, `solid`, `damage`, `speedModifier`. ⏳ Pending.
- **11e** File operations — `Ctrl+N/O/S` + Save As + Close + Exit, dirty-flag detection, confirmation prompts. ⏳ Pending.
- **11f** Validation — block save when a tile is missing skin/material/shape; warn on resource deletion when used. ⏳ Pending.
- **11g** About dialog (`F1`). ⏳ Pending.

## Phase 12 — UI-002 Structure Builder
Tool for assembling Structures from a Tile Library. Ref: [UI-002 - Structure Builder.md](Info/maps/UI-002%20-%20Structure%20Builder.md).

- **12a** Application skeleton — window, menus, toolbars, status bar, welcome screen. ⏳ Pending.
- **12b** Tile Library panel (loads `.tiles` file produced by Tile Builder) + Tile Preview. ⏳ Pending.
- **12c** Structure Viewport — 3D iso grid, place/erase/select tiles, Z-layer selector, origin marker `(0,0,0)`, toggleable grid. ⏳ Pending.
- **12d** Structure Properties panel — name, description, computed `Width/Depth/Height`, tile counts, Recalculate button. ⏳ Pending.
- **12e** Selected-tile info panel (relative coords, name, material, shape). ⏳ Pending.
- **12f** Undo / Redo stack. ⏳ Pending.
- **12g** File operations + validation (no empty structures, every used tile must exist in the library). ⏳ Pending.

## Phase 13 — UI-003 Map Builder
Final scenario authoring tool. Ref: [UI-003 - Map Builder.md](Info/maps/UI-003%20-%20Map%20Builder.md).

- **13a** Application skeleton — window, menus, toolbars, status bar, welcome screen. ⏳ Pending.
- **13b** `New Map Wizard` — name, width X, height Y, max Z, structure-library list, template selector (initially `Empty Map`), input validation. ⏳ Pending.
- **13c** Resource Browser — `Structures` tab + `Tiles` tab fed by libraries chosen in the wizard. ⏳ Pending.
- **13d** Map Viewport — `View` / `Edit` / `Select` tool modes, place structures + individual tiles, absolute coords. ⏳ Pending.
- **13e** Map Navigator (minimap) with viewport-rect indicator and click-to-jump. ⏳ Pending.
- **13f** Tool Options panel — per-tool dynamic UI (zoom/grid for View; resource picker + active Z for Edit; …). ⏳ Pending.
- **13g** Layer Controls — `Enable Layer View`, current Z up/down, "show only current layer" toggle. ⏳ Pending.
- **13h** Map Properties dialog + Validate Map + Statistics. ⏳ Pending.
- **13i** File operations (`.map` files compatible with the engine's v2 loader). ⏳ Pending.

## Phase 14 — Game ↔ Editor integration
Final wiring: the runtime consumes only artifacts authored by the editors.

- **14a** Engine bootstraps `SkinRegistry` / `MaterialRegistry` / `TileSetRegistry` from a manifest on startup. ⏳ Pending.
- **14b** Renderer reads sprites by `Skin` reference instead of `(TileMaterial, TileShape)`. ⏳ Pending.
- **14c** Replace `map_8.json` with editor-built `.map` artifacts; delete the legacy palette format once every map is migrated. ⏳ Pending.
- **14d** Smoke test: open the demo map produced by Map Builder, walk around, save with `F5`, reopen → byte-stable round-trip. ⏳ Pending.

## Future
Auto-tiling, tile variants, layer system (GROUND/DECORATION/OBJECT/EFFECT), spatial chunking, structure rotation/symmetry, prefab importers.
