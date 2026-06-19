# Phase 02 — Input y Movimiento del Jugador

## Objetivo

Añadir control por teclado y movimiento del jugador en coordenadas de mundo con detección de colisión contra tiles no caminables.

## Clases nuevas

| Clase | Paquete | Responsabilidad |
|---|---|---|
| `InputState` | `input` | Snapshot sincronizado del teclado (`boolean[]` indexado por VK) |
| `InputHandler` | `input` | `KeyAdapter` que delega en `InputState` |

## Clases modificadas

| Clase | Cambio |
|---|---|
| `Player` | `init(InputState, TileMap)` + lógica WASD + colisión |
| `GameEngine` | Instancia `InputState`, registra `InputHandler`, llama `player.init()` |
| `GameRenderer` | HUD ampliado con posición del jugador |

## Decisiones de diseño

### Separación EDT / game loop
`InputState` usa métodos `synchronized` para aislar la escritura (EDT) de la lectura (game-loop thread). No se pasan objetos Swing al bucle.

### Movimiento por ejes independientes
Los deltas de columna y fila se calculan y colisionan por separado, permitiendo deslizamiento diagonal (slide along walls).

### Colisión redondeada
La posición del jugador es `float`; se usa `Math.round()` para determinar el tile objetivo antes de comprobar `isWalkable()`.

### Mapeo diametrico WASD
```
W / ↑  →  col--   (noroeste de pantalla)
S / ↓  →  col++   (sureste de pantalla)
A / ←  →  row--   (noreste de pantalla)
D / →  →  row++   (suroeste de pantalla)
```

## HUD actualizado

```
FPS: 60  UPS: 60  POS: [3.0, 3.0]
```

## Resultado

El jugador se desplaza con WASD a 3 tiles/s independientemente del FPS. No puede atravesar tiles `WALL` ni `WATER`. La cámara sigue al jugador en tiempo real.
