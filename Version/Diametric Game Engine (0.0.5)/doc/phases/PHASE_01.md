# Phase 1 — Foundations

## Objective

Deliver a runnable Java/Maven project that:

1. Opens an 800×600 window with a fixed canvas.
2. Runs a decoupled game loop targeting 60 UPS and 60 FPS.
3. Renders a 7×7 demo map in **2:1 diametric** projection.
4. Shows the player as a red diamond and an FPS/UPS HUD.

## Implemented packages and classes

| Package                 | Class            | Purpose                                                    |
| ----------------------- | ---------------- | ---------------------------------------------------------- |
| `config`                | `GameConfig`     | Screen, tile, FPS/UPS constants and window title.          |
| `projection`            | `WorldPoint`     | Mutable `(col, row, z)` floats.                            |
| `projection`            | `ScreenPoint`    | Immutable `(x, y)` ints.                                   |
| `projection`            | `IsoProjection`  | World ↔ screen conversions.                                |
| `map`                   | `TileType`       | Enum (GRASS / WATER / WALL) with colour and walkability.   |
| `map`                   | `Tile`           | Wrapper over `TileType`.                                   |
| `map`                   | `TileLayer`      | `Tile[col][row]` grid.                                     |
| `map`                   | `TileMap`        | Holds the visual layer; `createDemoMap()` factory.         |
| `entity`                | `Entity`         | Abstract base with `WorldPoint` and `update(double)`.      |
| `entity`                | `Player`         | Concrete entity, default position `(3, 3, 0)`.             |
| `camera`                | `Camera`         | Centres viewport on a world target; translates `Graphics2D`. |
| `render`                | `TileRenderer`   | Draws tiles using painter's algorithm.                     |
| `render`                | `EntityRenderer` | Draws an entity placeholder marker.                        |
| `render`                | `GameRenderer`   | Orchestrates background → camera → tiles → entity → HUD.   |
| `core`                  | `GameState`      | Enum LOADING / RUNNING / PAUSED / STOPPED.                 |
| `core`                  | `GameWindow`     | `JFrame` + `Canvas` + `BufferStrategy(2)`.                 |
| `core`                  | `GameLoop`       | Runnable thread, accumulator-based UPS/FPS loop.           |
| `core`                  | `GameEngine`     | Wires every subsystem; `start()` / `update()` / `render()`. |
| (root)                  | `Application`    | `main` → `new GameEngine().start()`.                       |

## Decisions

- **No external dependencies** — only the JDK, easing the learning curve and reproducibility.
- **`Canvas` over `JPanel`** for the render surface so we control the `BufferStrategy` directly.
- **Camera-as-translation**: simpler than transforming every world coordinate, and keeps the world model untouched.
- **HUD** drawn last, in *screen space* (after restoring the original `AffineTransform`).
- **`createDemoMap()`** is a static factory rather than reading from disk — file-based maps land in Phase 7.

## Result

Running the JAR opens the window and prints `FPS: 60  UPS: 60` (or close to it) once per second on stdout. The diametric grid is visible, the wall border is grey, the inner area is green, and a single blue water tile sits at `(3, 3)` with a red player diamond drawn on top.

## Next phase

Phase 2 will add a `KeyListener`, a movement vector on `Player`, and an input-to-world axis mapping suited to diametric navigation.
