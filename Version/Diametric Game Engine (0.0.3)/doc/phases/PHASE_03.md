# Phase 3 — Multi-layer maps & Z elevation (corner-height system)

## Goals
- Introduce a vertical (Z) dimension into the diametric world.
- Support multiple tile layers per map (visual + object).
- Model **realistic elevation collisions**: a player cannot climb a vertical
  step; he must use a ramp.
- Render elevation with painter's-algorithm-friendly side faces.
- Allow **gradual climb** along ramps (no instant Z snap).

## Design — corner-height tiles

Each tile is modelled as a **diamond with 4 corners** (`N`, `E`, `S`, `W`),
each one having an integer height offset relative to the tile's own
`elevation` (Z base):

```
        N (top)
       /   \
     W       E
       \   /
        S (bottom)
```

Indices used in code:

```java
public static final int N = 0;
public static final int E = 1;
public static final int S = 2;
public static final int W = 3;
```

### `map.TileType`

14 types. Walkability and corner heights per type:

| Type        | Walkable | Color        | N | E | S | W | Notes                             |
|-------------|----------|--------------|---|---|---|---|-----------------------------------|
| `GRASS`     | yes      | green        | 0 | 0 | 0 | 0 | Flat ground                       |
| `WATER`     | no       | blue         | 0 | 0 | 0 | 0 | Blocked terrain                   |
| `WALL`      | no       | gray         | 0 | 0 | 0 | 0 | Blocked terrain                   |
| `FLOOR`     | yes      | light gray   | 0 | 0 | 0 | 0 | Flat indoor                       |
| `ELEVATED`  | yes      | dark brown   | 1 | 1 | 1 | 1 | Plateau (+1)                      |
| `CLIFF`     | no       | very dark    | 1 | 1 | 1 | 1 | Plateau but blocked               |
| `RAMP_NW`   | yes      | orange       | 1 | 0 | 0 | 1 | Edge ramp climbing toward NW edge |
| `RAMP_NE`   | yes      | orange       | 1 | 1 | 0 | 0 | Edge ramp climbing toward NE edge |
| `RAMP_SE`   | yes      | orange       | 0 | 1 | 1 | 0 | Edge ramp climbing toward SE edge |
| `RAMP_SW`   | yes      | orange       | 0 | 0 | 1 | 1 | Edge ramp climbing toward SW edge |
| `RAMP_N`    | yes      | orange       | 1 | 0 | 0 | 0 | Single-corner peak (N up)         |
| `RAMP_E`    | yes      | orange       | 0 | 1 | 0 | 0 | Single-corner peak (E up)         |
| `RAMP_S`    | yes      | orange       | 0 | 0 | 1 | 0 | Single-corner peak (S up)         |
| `RAMP_W`    | yes      | orange       | 0 | 0 | 0 | 1 | Single-corner peak (W up)         |

Removed in this iteration: the old `RAMP_UP` type and `getElevationDelta()`
API. Replaced by `getCornerHeight(int corner)`, `getCornerHeights()`,
`getMaxCornerHeight()`, `isRamp()`.

### `map.Tile`
- New `int elevation` field (default `0`, i.e. ground level).
- Constructor overload `Tile(type, elevation)`.

### `map.TileMap`
- Holds two `TileLayer`s: `visualLayer` (terrain) and `objectLayer` (props).
- Demo map enlarged to **13 × 11** to showcase every ramp type:
  - `RAMP_NW (2,2)` → `ELEVATED (1,2)`
  - `RAMP_NE (5,2)` → `ELEVATED (5,1)`
  - `RAMP_SE (9,5)` → `ELEVATED (10,5)`
  - `RAMP_SW (5,8)` → `ELEVATED (5,9)`
  - Single-corner ramps: `RAMP_N (8,2)`, `RAMP_S (10,2)`, `RAMP_E (8,8)`, `RAMP_W (10,8)`.
  - Water tiles at `(3,5)` and `(7,5)`.

### `projection.IsoProjection`
- Overloads:
  - `worldToScreen(col, row, z)`
  - `worldToScreen(WorldPoint)`
  - `worldToScreen(Tile, col, row)`
- Formula:
  - `screenX = (col − row) · 32`
  - `screenY = (col + row) · 16 − z · 32`

### `render.TileRenderer`
- Each tile corner is **projected independently** using
  `tile.elevation + type.getCornerHeight(corner)`. Ramps therefore appear
  visibly inclined.
- Two side faces per tile are drawn (E↔S edge and S↔W edge) connecting the
  top corners down to ground level (`z = 0`):
  - SW face — darken factor `0.55`.
  - SE face — darken factor `0.75`.
- Iterates `visualLayer` then `objectLayer` with a shared `drawCell()` helper
  in painter's-algorithm row order.

### `entity.Player` — collision rules

After per-axis movement, candidate cells (`fromCol,fromRow` →
`toCol,toRow`) are validated by `isMoveAllowed()`:

1. The destination tile must exist and be `walkable`.
2. The **two corners shared on the transition edge** (one corner from the
   source tile and one from the target tile, then a second pair) must have
   the **same absolute height**.

Direction → shared-corner mapping (source / target / source / target):

| Δcol | Δrow | Direction | Shared corners                 |
|------|------|-----------|--------------------------------|
| `+1` | `0`  | EAST      | E↔N , S↔W                      |
| `−1` | `0`  | WEST      | N↔E , W↔S                      |
| `0`  | `+1` | SOUTH     | S↔E , W↔N                      |
| `0`  | `−1` | NORTH     | N↔W , E↔S                      |

This rule **naturally blocks** a flat→plateau transition (corner heights
0 vs 1 on the shared edge) and **forces** the player to take a ramp whose
sloped edge matches both neighbours.

### `entity.Player` — gradual Z

`elevationOf(col, row)` returns a smooth Z by **bilinear interpolation** of
the four corner heights inside the current tile. With the diamond mapped
to UV space (N→(0,0), E→(1,0), S→(1,1), W→(0,1)) and fractional position
inside the tile (`u = (col − c) + 0.5`, `v = (row − r) + 0.5`), the climb
along a ramp is continuous instead of an instant +1 step.

### `render.GameRenderer`
- HUD upgraded to: `FPS: %d  UPS: %d  POS: [col, row, z]`.

## Verification checklist
- [x] `mvn -q compile` and `mvn -q package` succeed.
- [x] All 8 ramps render with visible incline.
- [x] Player gradually climbs ramps (HUD `z` interpolates).
- [x] Player cannot enter `ELEVATED` directly from `GRASS`.
- [x] Player can step back down via any matching ramp.
- [x] `WATER` and `CLIFF` block movement.

## Run (development mode)
```powershell
mvn -q compile exec:java
```
