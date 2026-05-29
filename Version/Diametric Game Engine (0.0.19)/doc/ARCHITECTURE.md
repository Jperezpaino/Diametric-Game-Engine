# Architecture

The engine is split into small, single-responsibility packages. Logic and rendering are strictly decoupled.

## Module diagram (Phase 1)

```
                  +-------------------+
                  |   Application     |
                  +---------+---------+
                            |
                            v
                  +-------------------+
                  |    GameEngine     |
                  +----+---+----+-----+
                       |   |    |
        +--------------+   |    +--------------+
        v                  v                   v
   +---------+        +---------+         +-----------+
   | GameLoop|        |GameWindow|        |GameRenderer|
   +----+----+        +----+-----+        +-----+------+
        |                  |                    |
        | update / render  | Graphics2D         | renders
        v                  v                    v
   +---------+        +---------+         +-----------+
   | Player  |<------>| Camera  |<--------|TileRenderer|
   +----+----+        +----+----+         +-----+------+
        |                  |                    |
        v                  v                    v
   +---------+        +---------+         +-----------+
   |WorldPoint|       |IsoProj. |         | TileMap   |
   +---------+        +---------+         +-----------+
```

## Packages

| Package                  | Responsibility                                                       |
| ------------------------ | -------------------------------------------------------------------- |
| `es.noa.rad`             | Application entry point.                                             |
| `es.noa.rad.config`      | Compile-time constants (screen, tile, FPS/UPS).                      |
| `es.noa.rad.core`        | Engine lifecycle: window, loop, state, orchestrator.                 |
| `es.noa.rad.projection`  | World ↔ screen coordinate maths (2:1 diametric).                     |
| `es.noa.rad.map`         | Tiles, layers and the in-memory tile map.                            |
| `es.noa.rad.entity`      | Game entities (`Entity`, `Player`).                                  |
| `es.noa.rad.camera`      | Viewport that follows a target world position.                       |
| `es.noa.rad.render`      | Drawing primitives: tiles, entities and HUD.                         |

## Key Phase 1 decisions

1. **Pure `Graphics2D` + `Canvas` + `BufferStrategy(2)`** for double-buffered rendering — no external libraries.
2. **Decoupled UPS/FPS** loop with a fixed 60/60 target and an accumulator pattern.
3. **Painter's algorithm** (row-outer, col-inner) guarantees correct depth ordering for the diametric view.
4. **Mutable `WorldPoint`** to avoid allocation churn in hot paths (entity update, camera follow).
5. **Immutable `ScreenPoint`** to keep render code side-effect free.
6. **`Camera` translates the `Graphics2D`** instead of moving the world; tile coords stay authoritative.
