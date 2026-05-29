# Phase 3 — TileMaterial / TileShape split + voxel Z stacking

> **Estado:** ✅ completada (mayo 2026)

## Objetivo

Reorganizar el sistema de tiles para separar **gameplay** de **geometría**, y
preparar el motor para mundos con altura real (voxel) y reglas de movimiento
ricas (rampas, esquinas, materiales con efectos).

## Decisiones de arquitectura

### 1. Split `TileType` → `TileMaterial` + `TileShape`

| Antes | Ahora |
|---|---|
| `enum TileType` mezclaba color, walkable, alturas de esquina y `elevationDelta` | `enum TileMaterial` (color, walkable, speedFactor, damagePerSecond, drowning) + `enum TileShape` (geometría) |

Un `Tile` ahora es un **record inmutable** `(TileMaterial, TileShape, int elevation)`.

### 2. `TileShape` con `edgeMask` 4-bit

Las 14 formas se codifican como un bitmask sobre los 4 lados (N=8, E=4, S=2, W=1):

| Shape | Mask | Lados altos |
|---|---|---|
| `FLOOR` | `0b0000` | – |
| `BLOCK` | `0b1111` | N+E+S+W |
| `DOUBLE_RAMP_N/E/S/W` | un solo bit | el lado opuesto al "pie" de la rampa |
| `RAMP_SW/NW/NE/SE` | dos bits adyacentes | esquina alta opuesta al nombre |
| `CONCAVE_N/E/S/W` | tres bits | el lado bajo da nombre |

`shape.cornerHeight(Corner)` devuelve `min(edgeHeight(side1), edgeHeight(side2))`
de los dos lados adyacentes a esa esquina, lo que produce automáticamente la
geometría correcta de cualquier vértice de la cara superior.

### 3. Voxel `TileLayer`

`TileLayer` es ahora un grid 3D **disperso**: `TreeMap<Integer,Tile>` por
`(col,row)` indexado por Z. Esto permite apilar tiles en altura con coste de
memoria proporcional sólo a las celdas pobladas.

API clave:

```java
layer.setTile(col, row, z, material, shape);
layer.getTopTile(col, row);           // tile más alto
layer.getFloorTileAt(col, row, z);    // tile bajo el actor
layer.forEachTile(visitor);           // recorrido pintor
```

### 4. Reglas de movimiento extraídas

Toda la lógica de "puedo ir de A a B" vive en `map.rules.MovementValidator`:

- **Cardinal**: la altura absoluta del borde compartido debe coincidir.
- **Diagonal**: ambos cardinales intermedios deben ser legales (no
  *corner-cutting*) y la esquina compartida debe coincidir.
- **Walk-on-block**: un `BLOCK` sin tile encima es transitable por arriba.

### 5. Efectos de terreno

`map.rules.TerrainEffects` aplica multiplicadores de velocidad y daño:

```
combinedSpeedFactor = material.speedFactor
                    * (diagonal     ? 0.85 : 1.0)
                    * (climbingUp   ? 0.75 : 1.0)
```

### 6. Proyección — `Z_STEP_PX = 16`

`GameConfig.TILE_HEIGHT = 32` sigue siendo la **media-altura visual del rombo**.
Se añade `GameConfig.Z_STEP_PX = 16` (la mitad) como **paso vertical por nivel
de elevación**, conforme a los assets de referencia (`doc/Info/Reference.json`).

```
screenX = (col - row) * (TILE_WIDTH  / 2)
screenY = (col + row) * (TILE_HEIGHT / 2) - z * Z_STEP_PX
```

`IsoProjection.projectCorner(tile, col, row, corner)` proyecta cada uno de los
4 vértices superiores combinando elevación de la tile + altura de esquina del
shape.

### 7. Direcciones tipadas

- `EdgeSide` (N/E/S/W) con `dCol`, `dRow`, `opposite()`.
- `Direction8` (8 direcciones) con `isDiagonal`, `horizontalComponent`,
  `verticalComponent`, `dCol`, `dRow`.

## Catálogo de materiales (provisional)

| Material | Walkable | Speed | DPS | Drown |
|---|---|---|---|---|
| GRASS | ✅ | 1.00 | 0 | – |
| STONE | ✅ | 1.00 | 0 | – |
| WOOD  | ✅ | 1.00 | 0 | – |
| SAND  | ✅ | 0.85 | 0 | – |
| MUD   | ✅ | 0.50 | 0 | – |
| SNOW  | ✅ | 0.75 | 0 | – |
| WATER | ❌ | 0.40 | 0 | ✅ |
| LAVA  | ❌ | 0.30 | 5 | – |

## Demo (`TileMap.createDemoMap`)

Mapa 16×16 que muestra:

- Carpet de `GRASS FLOOR` a z=0.
- Fila de las 14 shapes (col 1..14, row 6) sobre base `STONE FLOOR`.
- 5 `WOOD BLOCK` (col 2..6, row 10) para probar walk-on-top.
- Charco de `WATER` (col 8-9, row 12) y parche de `LAVA` (col 12, row 12).

## HUD

`GameRenderer` muestra dos líneas:

```
FPS: 60  UPS: 60  POS: [4.2, 6.0, 1.0]
CELL: [4, 6]  MAT: STONE  SHAPE: DOUBLE_RAMP_N
```

## Archivos nuevos / refactorizados

| Archivo | Estado |
|---|---|
| `map/EdgeSide.java` | nuevo |
| `map/Direction8.java` | nuevo |
| `map/TileShape.java` | nuevo (sustituye geometría de `TileType`) |
| `map/TileMaterial.java` | nuevo (sustituye gameplay de `TileType`) |
| `map/Tile.java` | reescrito como `record` |
| `map/TileLayer.java` | reescrito a voxel disperso |
| `map/TileMap.java` | reescrito (un solo voxel layer + demo) |
| `map/rules/MovementValidator.java` | nuevo |
| `map/rules/TerrainEffects.java` | nuevo |
| `entity/Player.java` | usa `MovementValidator` + `TerrainEffects` |
| `render/TileRenderer.java` | usa `cornerHeight` + caras laterales |
| `render/GameRenderer.java` | HUD ampliado con material/shape/cell |
| `projection/IsoProjection.java` | usa `Z_STEP_PX` + `projectCorner` |
| `config/GameConfig.java` | añade `Z_STEP_PX = 16` |
| `map/TileType.java` | **eliminado** |

## Pendiente (futuras fases)

- **Fase 4 — Cámara**: scroll, zoom, multi-nivel.
- Restricción real de materiales según shape (ahora libre por código).
- Sprites en lugar de polígonos planos.
- Aplicación real de `damagePerSecond` y drowning sobre el `Player`.
- Animación de subida/bajada en rampas (ahora snap por bilineal).