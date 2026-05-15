GitHub Copilot: # Diametric Game Engine — Arquitectura Técnica y Plan de Implementación

---

## 1. Resumen Ejecutivo

La estrategia recomendada es **construcción incremental por capas**, empezando desde el núcleo más pequeño posible que sea compilable y visible, y añadiendo capacidades en cada fase sin romper lo anterior.

El motor se construirá sobre tres pilares:
- **Game Loop desacoplado**: lógica (UPS) y render (FPS) independientes.
- **Proyección diamétrica como ciudadano de primera clase**: todas las decisiones de coordenadas se toman desde el inicio pensando en la conversión mundo↔pantalla.
- **Separación estricta lógica/render**: las entidades no saben cómo se dibujan; los renderers no saben cómo funciona la lógica.

No se usará ningún framework externo. Todo se construye sobre `javax.swing`, `java.awt` y `java.awt.Graphics2D`.

El enfoque evita sobrediseño: no hay inyección de dependencias, no hay ECS complejo, no hay reflección. Se empieza con estructuras simples y se refactoriza solo cuando el crecimiento lo justifica.

---

## 2. Arquitectura General

```
┌─────────────────────────────────────────────────────┐
│                     GameEngine                       │
│  (coordina ciclo de vida: init→load→loop→shutdown)  │
└────────────────────┬────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
   GameLoop      GameState     Config
  (UPS + FPS)   (estado actual) (resolución, fps, ups)
        │
   ┌────┴────┐
   ▼         ▼
update()   render()
   │         │
   │    ┌────┴──────────────┐
   │    ▼                   ▼
   │  Renderer          Camera
   │  (Graphics2D)      (offset mundo→pantalla)
   │         │
   │    IsoProjection
   │    (conversión coords)
   │
   ├── InputHandler
   ├── World (mapa + entidades)
   │     ├── TileMap
   │     │     └── TileLayer[]
   │     └── EntityManager
   │           ├── Player
   │           └── Enemy[]
   ├── CollisionSystem
   ├── PathfindingSystem (A*)
   └── EventSystem
```

**Principio de diseño**: cada subsistema tiene una responsabilidad clara. El `GameEngine` es el único punto de entrada y coordina, pero no implementa lógica de juego.

---

## 3. Módulos del Sistema

### 3.1 `core` — Núcleo del Motor
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Ciclo de vida, game loop, ventana, configuración |
| **Problema que resuelve** | Arrancar el motor, mantener el bucle estable, gestionar estados |
| **Clases principales** | `GameEngine`, `GameLoop`, `GameWindow`, `GameConfig`, `GameState` |
| **Dependencias** | Ninguna (base de todo) |
| **Orden** | **1º — primero** |

### 3.2 `input` — Entrada de Usuario
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Capturar y exponer el estado de teclado y ratón |
| **Problema que resuelve** | Desacoplar eventos AWT del juego; saber qué está pulsado en cada frame |
| **Clases principales** | `InputHandler`, `KeyboardState`, `MouseState` |
| **Dependencias** | `core` |
| **Orden** | **2º** |

### 3.3 `render` — Renderizado
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Dibujar el mundo, entidades e interfaz con Graphics2D |
| **Problema que resuelve** | Separar completamente el pintado de la lógica |
| **Clases principales** | `GameRenderer`, `IsoProjection`, `TileRenderer`, `EntityRenderer`, `DebugRenderer` |
| **Dependencias** | `core`, `map`, `entity`, `camera` |
| **Orden** | **3º** |

### 3.4 `projection` — Proyección Diamétrica
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Conversión coordenadas mundo (col, row, z) ↔ pantalla (px, py) |
| **Problema que resuelve** | Centralizar todas las matemáticas de proyección en un único lugar |
| **Clases principales** | `IsoProjection` (estática o singleton), `WorldPoint`, `ScreenPoint` |
| **Dependencias** | Ninguna |
| **Orden** | **2º (junto con render)** |

### 3.5 `map` — Mapa y Tiles
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Representar el mapa, sus capas y celdas |
| **Problema que resuelve** | Almacenar tiles, capas de colisión, altura, eventos del mapa |
| **Clases principales** | `TileMap`, `TileLayer`, `Tile`, `TileType`, `MapLoader` |
| **Dependencias** | `projection` |
| **Orden** | **3º** |

### 3.6 `camera` — Cámara
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Calcular el desplazamiento para centrar al jugador con límites de mapa |
| **Problema que resuelve** | Separar posición del mundo de posición en pantalla |
| **Clases principales** | `Camera` |
| **Dependencias** | `map`, `projection` |
| **Orden** | **4º** |

### 3.7 `entity` — Entidades
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Definir, gestionar y actualizar entidades del mundo |
| **Problema que resuelve** | Modelar jugador, enemigos y objetos con posición y comportamiento |
| **Clases principales** | `Entity`, `Player`, `Enemy`, `EntityManager` |
| **Dependencias** | `map`, `input`, `collision` |
| **Orden** | **4º** |

### 3.8 `collision` — Colisiones
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Detectar y resolver colisiones entre entidades y el mapa |
| **Problema que resuelve** | Evitar que entidades atraviesen tiles sólidos o paredes |
| **Clases principales** | `CollisionSystem`, `CollisionLayer` |
| **Dependencias** | `map`, `entity` |
| **Orden** | **5º** |

### 3.9 `ai` — Enemigos y Pathfinding
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Comportamiento de enemigos y navegación A* |
| **Problema que resuelve** | Enemigos que detectan y persiguen al jugador por rutas válidas |
| **Clases principales** | `AStarPathfinder`, `PathNode`, `EnemyAI` |
| **Dependencias** | `map`, `entity`, `collision` |
| **Orden** | **6º** |

### 3.10 `event` — Eventos e Interacciones
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Disparar acciones al interactuar con tiles o zonas del mapa |
| **Problema que resuelve** | Cambios de mapa, interruptores, triggers de eventos |
| **Clases principales** | `EventSystem`, `MapEvent`, `EventTrigger`, `EventAction` |
| **Dependencias** | `map`, `entity` |
| **Orden** | **6º** |

### 3.11 `config` — Configuración
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Cargar y exponer parámetros de configuración |
| **Problema que resuelve** | Resolución, FPS, UPS, tamaño de tile, sin hardcodear valores |
| **Clases principales** | `GameConfig`, `ConfigLoader` |
| **Dependencias** | Ninguna |
| **Orden** | **1º** |

### 3.12 `resource` — Gestión de Recursos
| Aspecto | Detalle |
|---|---|
| **Responsabilidad** | Cargar y cachear imágenes, fuentes y datos |
| **Problema que resuelve** | Acceso centralizado a recursos, sin cargas duplicadas |
| **Clases principales** | `ResourceManager`, `ImageCache` |
| **Dependencias** | Ninguna |
| **Orden** | **Fase 2** |

---

## 4. Estructura de Paquetes

```
es.noa.rad/
├── Application.java                  ← punto de entrada
│
├── core/
│   ├── GameEngine.java
│   ├── GameLoop.java
│   ├── GameWindow.java
│   └── GameState.java                ← enum: LOADING, RUNNING, PAUSED, STOPPED
│
├── config/
│   ├── GameConfig.java
│   └── ConfigLoader.java
│
├── input/
│   ├── InputHandler.java
│   ├── KeyboardState.java
│   └── MouseState.java
│
├── projection/
│   ├── IsoProjection.java
│   ├── WorldPoint.java               ← (col, row, z) en espacio mundo
│   └── ScreenPoint.java              ← (x, y) en píxeles pantalla
│
├── map/
│   ├── TileMap.java
│   ├── TileLayer.java
│   ├── Tile.java
│   ├── TileType.java                 ← enum con propiedades
│   └── MapLoader.java
│
├── camera/
│   └── Camera.java
│
├── entity/
│   ├── Entity.java                   ← base abstracta
│   ├── Player.java
│   ├── Enemy.java
│   └── EntityManager.java
│
├── collision/
│   ├── CollisionSystem.java
│   └── CollisionLayer.java
│
├── ai/
│   ├── AStarPathfinder.java
│   ├── PathNode.java
│   └── EnemyAI.java
│
├── event/
│   ├── EventSystem.java
│   ├── MapEvent.java
│   ├── EventTrigger.java
│   └── EventAction.java
│
├── render/
│   ├── GameRenderer.java
│   ├── TileRenderer.java
│   ├── EntityRenderer.java
│   └── DebugRenderer.java
│
└── resource/
    ├── ResourceManager.java
    └── ImageCache.java
```

```
doc/
├── README.md
├── ARCHITECTURE.md
├── ROADMAP.md
├── MODULES.md
├── PROJECTION.md
├── MAP_FORMAT.md
└── phases/
    ├── PHASE_01.md
    ├── PHASE_02.md
    └── ...
```

---

## 5. Clases e Interfaces Base Mínimas

| Clase / Interfaz | Propósito | Responsabilidad | Relaciones | Prioridad |
|---|---|---|---|---|
| `GameConfig` | Parámetros globales | Resolución, FPS, UPS, tamaño tile | Usada por todos | **Inmediata** |
| `GameEngine` | Orquestador principal | Init, load, loop, shutdown | Contiene `GameLoop`, `GameWindow` | **Inmediata** |
| `GameLoop` | Bucle principal | Separar update (UPS) y render (FPS) | Llama a `GameEngine.update/render` | **Inmediata** |
| `GameWindow` | Ventana Swing | Crear JFrame + JPanel, exponer Graphics2D | Usado por `GameEngine` | **Inmediata** |
| `GameState` | Estado del motor | Enum: LOADING, RUNNING, PAUSED, STOPPED | Consultado por `GameLoop` | **Inmediata** |
| `InputHandler` | Captura de entrada | KeyListener + MouseListener, estado por frame | Usado por `Player` | **Inmediata** |
| `IsoProjection` | Matemáticas de proyección | worldToScreen, screenToWorld | Usado por render y cámara | **Inmediata** |
| `WorldPoint` | Coordenada mundo | Contiene col, row, z (float) | Usado por entidades y mapa | **Inmediata** |
| `Camera` | Desplazamiento de vista | Calcula offset, aplica límites de mapa | Usa `IsoProjection`, `TileMap` | **Inmediata** |
| `TileMap` | Estructura del mapa | Contiene capas, dimensiones | Contiene `TileLayer[]` | **Inmediata** |
| `TileLayer` | Capa individual | Array 2D de `Tile` | Pertenece a `TileMap` | **Inmediata** |
| `Tile` | Celda del mapa | TileType, altura, walkable, evento | Contenido en `TileLayer` | **Inmediata** |
| `TileType` | Tipos de tile | Enum con color debug, walkable, sólido | Usado por `Tile` | **Inmediata** |
| `Entity` | Base de entidades | Posición, velocidad, update abstracto | Extendida por Player, Enemy | **Inmediata** |
| `Player` | Jugador | Movimiento, input, estado | Extiende `Entity`, usa `InputHandler` | **Inmediata** |
| `GameRenderer` | Renderizador principal | Coordina render de mapa + entidades | Usa `TileRenderer`, `EntityRenderer` | **Inmediata** |
| `TileRenderer` | Dibujo de tiles | Pinta cada celda con Graphics2D | Usa `IsoProjection`, `Camera` | **Inmediata** |
| `EntityRenderer` | Dibujo de entidades | Pinta entidades en posición de pantalla | Usa `IsoProjection`, `Camera` | **Inmediata** |
| `CollisionSystem` | Detección colisiones | Comprueba walkable en mapa | Usa `TileMap`, `Entity` | **Siguiente fase** |
| `Enemy` | Enemigo | Detección, movimiento, AI | Extiende `Entity`, usa `EnemyAI` | **Siguiente fase** |
| `AStarPathfinder` | Pathfinding | A* sobre TileMap | Usa `TileMap` | **Posterior** |
| `EventSystem` | Eventos de mapa | Dispara acciones por trigger | Usa `TileMap`, `Entity` | **Posterior** |
| `ResourceManager` | Caché de recursos | Carga y devuelve imágenes/fuentes | Usado por renderers | **Posterior** |

---

## 6. Decisiones Técnicas Clave

### 6.1 Sistema de Coordenadas del Mundo
**Decisión**: coordenadas en **columna/fila flotantes** (`float col, row, z`).
- `col` y `row` son índices de celda (con decimales para posición dentro de la celda).
- `z` representa altura en unidades de tile (0 = suelo).
- **Por qué**: mantener todo en espacio de celda simplifica colisiones, A* y conversión.

### 6.2 Conversión Mundo → Pantalla (Proyección 2:1)
```
screenX = (col - row) * (tileWidth / 2)
screenY = (col + row) * (tileHeight / 2) - z * tileHeight
```
Donde `tileWidth = 2 * tileHeight` (relación 2:1).

**Centralizado en `IsoProjection`**. Nadie más hace esta matemática directamente.

### 6.3 Conversión Pantalla → Mundo (Inversa)
```
col = (screenX / (tileWidth/2) + screenY / (tileHeight/2)) / 2
row = (screenY / (tileHeight/2) - screenX / (tileWidth/2)) / 2
```
Necesaria para clicks de ratón. Se implementa desde el inicio en `IsoProjection`.

### 6.4 Orden de Pintado (Painter's Algorithm)
**Decisión**: pintar tiles de **menor a mayor (row + col)**, de fondo a frente.
- Iterar primero por `row` (de 0 a maxRow), luego por `col` dentro de cada fila.
- Las entidades se intercalan en el orden de pintado según su `row + col`.
- **Por qué**: es el orden natural de profundidad en proyección diamétrica 2:1.

⚠️ **Riesgo crítico**: si se pinta en orden incorrecto, los tiles se superponen mal. Fijar esto en la fase 1.

### 6.5 Separación Lógica / Render
- `Entity.update()` no toca Graphics2D nunca.
- `EntityRenderer.render(Entity, Graphics2D, Camera)` no modifica estado de entidad.
- **Por qué**: permite en el futuro añadir sprites, animaciones o efectos sin tocar lógica.

### 6.6 Estructura Interna del Mapa
```
TileMap
  ├── int width, depth, height   (col, row, z máximos)
  ├── TileLayer visualLayer      (lo que se ve)
  ├── TileLayer collisionLayer   (walkable/sólido)
  ├── TileLayer heightLayer      (z de cada celda)
  └── TileLayer eventLayer       (triggers)
```
Capas separadas desde el principio. En fase 1 solo se usa `visualLayer` y `collisionLayer`.

### 6.7 Colisiones
**Decisión**: colisión por celda (tile-based), no por bounding box de píxeles.
- Comprobar si la celda destino es `walkable` antes de mover.
- **Por qué**: simple, predecible y coherente con el espacio de coordenadas mundo.
- En fases posteriores se puede añadir colisión de sub-celda si se necesita.

### 6.8 Cámara
**Decisión**: la cámara trabaja en **píxeles de pantalla** (offset x, y).
- Se calcula convirtiendo posición del jugador a pantalla y restando el centro de la ventana.
- Se aplican límites para no mostrar fuera del mapa.
- **Por qué**: más simple que trabajar en coordenadas mundo para el desplazamiento de vista.

### 6.9 Game Loop
**Decisión**: loop de hilo propio con **UPS fijo y FPS desacoplado**.
```java
// Pseudocódigo
double updateInterval = 1e9 / ups;
double renderInterval = 1e9 / fps;
// acumular tiempo, llamar update() cuando toca, render() cuando toca
```
Interpolación de render: en fase inicial sin interpolación. Se añade en fase 3.

### 6.10 Pathfinding
**Decisión**: A* sobre grilla de tiles, trabajando en coordenadas enteras `(col, row)`.
- Heurística: distancia Manhattan (sin diagonal en primera versión).
- Se ejecuta solo cuando el enemigo necesita recalcular ruta (no cada frame).

### 6.11 Eventos
**Decisión**: eventos definidos en `eventLayer` como `TileType` especial + `MapEvent` asociado.
- Al entrar en una celda con evento, `EventSystem` lo procesa.
- Acciones posibles: cambio de mapa, activar interruptor, mensaje.

---

## 7. Roadmap por Fases

### Fase 1 — Ventana, Loop y Mapa Básico
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Ventana abierta con mapa diamétrico pintado y personaje visible |
| **Funcionalidades** | Ventana, game loop, proyección iso, mapa hardcodeado, personaje como rombo |
| **Módulos** | `core`, `config`, `projection`, `map`, `render`, `entity` (Player básico) |
| **Clases principales** | `GameEngine`, `GameLoop`, `GameWindow`, `GameConfig`, `IsoProjection`, `TileMap`, `TileLayer`, `Tile`, `TileType`, `GameRenderer`, `TileRenderer`, `EntityRenderer`, `Player` |
| **Resultado visible** | Ventana con cuadrícula diamétrica y un rombo que representa al jugador |
| **Criterio de fin** | Compila, ejecuta, se ve el mapa y el jugador |
| **Doc a completar** | `README.md`, `ARCHITECTURE.md`, `PHASE_01.md`, Javadoc en todas las clases |

### Fase 2 — Input, Movimiento y Cámara
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Jugador se mueve por el mapa, cámara lo sigue |
| **Funcionalidades** | Teclado, movimiento en espacio mundo, cámara con límites, colisión básica |
| **Módulos** | `input`, `camera`, `collision` |
| **Clases principales** | `InputHandler`, `KeyboardState`, `Camera`, `CollisionSystem` |
| **Resultado visible** | Jugador se mueve con WASD/flechas, cámara lo sigue, no atraviesa tiles sólidos |
| **Criterio de fin** | Movimiento fluido, colisión funcional, cámara correcta |
| **Doc a completar** | `PHASE_02.md`, doc de `Camera`, `InputHandler`, `CollisionSystem` |

### Fase 3 — Mapa desde Fichero y Capas
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Cargar mapas desde datos externos (array/JSON simple), múltiples capas |
| **Funcionalidades** | `MapLoader`, capas visual/colisión/altura, tiles con propiedades |
| **Módulos** | `map` (completo), `config` |
| **Clases principales** | `MapLoader`, `TileLayer` (altura, colisión), `TileType` extendido |
| **Resultado visible** | Mapa cargado desde fichero, áreas intransitables visibles |
| **Criterio de fin** | Mapa más grande que pantalla, con zonas bloqueadas |
| **Doc a completar** | `MAP_FORMAT.md`, `PHASE_03.md` |

### Fase 4 — Enemigos y Detección
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Enemigos en el mapa que detectan y persiguen al jugador |
| **Funcionalidades** | `Enemy`, `EnemyAI` con radio de detección, movimiento simple |
| **Módulos** | `entity`, `ai` (básico), `collision` |
| **Clases principales** | `Enemy`, `EnemyAI`, `EntityManager` |
| **Resultado visible** | Enemigos que patrullan y persiguen al entrar en rango |
| **Criterio de fin** | Al menos 3 enemigos funcionales con detección |
| **Doc a completar** | `PHASE_04.md`, doc `EnemyAI` |

### Fase 5 — Pathfinding A*
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Enemigos navegan por el mapa evitando obstáculos |
| **Funcionalidades** | A* completo sobre `collisionLayer` |
| **Módulos** | `ai` |
| **Clases principales** | `AStarPathfinder`, `PathNode` |
| **Resultado visible** | Enemigo rodea obstáculos para llegar al jugador |
| **Criterio de fin** | A* funcional, enemigo sigue ruta correcta |
| **Doc a completar** | `PHASE_05.md`, doc `AStarPathfinder` |

### Fase 6 — Eventos e Interacciones
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Triggers de mapa: puertas, interruptores, cambio de mapa |
| **Funcionalidades** | `EventSystem`, `MapEvent`, `EventTrigger`, `EventAction` |
| **Módulos** | `event` |
| **Clases principales** | `EventSystem`, `MapEvent`, `EventTrigger` |
| **Resultado visible** | Al entrar en una celda especial, se ejecuta una acción |
| **Criterio de fin** | Al menos cambio de mapa y activación de interruptor funcionales |
| **Doc a completar** | `PHASE_06.md`, doc sistema de eventos |

### Fase 7 — Sprites, Animaciones y Recursos
| Aspecto | Detalle |
|---|---|
| **Objetivo** | Sustituir dibujo primitivo por imágenes reales |
| **Funcionalidades** | `ResourceManager`, `ImageCache`, animaciones por frames |
| **Módulos** | `resource`, `render` (ampliado) |
| **Clases principales** | `ResourceManager`, `AnimationController`, `SpriteSheet` |
| **Resultado visible** | Tiles y personaje con sprites reales |
| **Criterio de fin** | Motor renderiza imágenes y animaciones básicas |
| **Doc a completar** | `PHASE_07.md`, doc `ResourceManager` |

---

## 8. Definición del MVP

### ✅ Imprescindible para el MVP
- Ventana funcional con game loop estable
- Proyección diamétrica 2:1 correcta
- Mapa básico renderizado (visual + colisión)
- Jugador movible con teclado
- Cámara que sigue al jugador con límites
- Colisión básica tile-based

### ⚠️ Importante pero no esencial en MVP
- Carga de mapa desde fichero
- Enemigos con detección
- Múltiples capas de mapa
- Conversión pantalla→mundo (click de ratón)

### 🔮 Ampliaciones posteriores
- A* y pathfinding
- Sistema de eventos
- Sprites e imágenes reales
- Animaciones
- Audio
- Partículas, iluminación
- IA avanzada
- Guardado/carga de partida

---

## 9. Riesgos Técnicos

### 🔴 Crítico: Orden de Pintado en Proyección Diamétrica
Si los tiles se pintan en orden incorrecto, las superposiciones visuales serán incorrectas.
**Solución**: implementar desde el inicio el orden por `(row + col)` creciente y nunca saltarse este contrato.

### 🔴 Crítico: Mezcla de Espacios de Coordenadas
Confundir coordenadas mundo (celda) con coordenadas pantalla (píxel) genera bugs imposibles de rastrear.
**Solución**: `WorldPoint` y `ScreenPoint` como tipos distintos. `IsoProjection` es la única que convierte. Nunca hacer la matemática inline fuera de ella.

### 🟠 Alto: Cálculo de Profundidad con Altura
Cuando los tiles tienen `z > 0`, el orden de pintado simple falla porque una pared alta puede tapar elementos visualmente delante de ella.
**Solución fase 1**: ignorar altura en el orden de pintado. En fase 3 implementar profundidad corregida por z: `depth = row + col + z * factor`.

### 🟠 Alto: Sincronización Update/Render
Si el render lee estado mientras update lo modifica, puede haber artefactos visuales.
**Solución**: en fase 1, update y render en el mismo hilo. Si en fases avanzadas se separan, usar doble buffer de estado.

### 🟡 Medio: Colisiones con Desniveles
Colisión tile-based simple no maneja rampas ni desniveles bien.
**Solución**: en MVP usar solo `walkable/solid`. Altura y rampas son fase 3+.

### 🟡 Medio: Escalabilidad del Sistema de Entidades
Una lista plana de entidades se vuelve lenta con muchas entidades.
**Solución**: `EntityManager` con separación por tipo desde el inicio. Si se necesita más rendimiento, añadir particionado espacial (cuadrícula) en fase posterior.

### 🟡 Medio: Pathfinding en Mapas Grandes
A* sin optimización es costoso en mapas grandes.
**Solución**: calcular ruta solo cuando el objetivo cambia, no cada frame. Limitar radio de búsqueda.

### 🟢 Bajo pero vigilar: Acoplamiento Render/Lógica
Si las entidades empiezan a tener referencias a Graphics2D, el motor se vuelve imposible de mantener.
**Solución**: el contrato `Entity` nunca tiene métodos de render. Es una regla de arquitectura, no una restricción técnica.

---

## 10. Recomendaciones Prácticas

### Organización del Código
- Un fichero = una clase. Sin clases anidadas salvo casos muy justificados.
- Constantes del motor en `GameConfig`. Cero hardcoding fuera de ella.
- Getters/setters solo donde sean necesarios. No generar por defecto.

### Modularidad
- Cada módulo se puede compilar y razonar de forma independiente.
- Las dependencias van hacia abajo: `render` depende de `map`, no al revés.
- Usar interfaces cuando haya más de una implementación posible (`Renderable`, `Updatable`).

### Rendimiento con Graphics2D
- Usar `BufferStrategy` (doble o triple buffer) en lugar de `paintComponent` directo.
- Nunca crear objetos `Color`, `Font` o `BasicStroke` dentro del bucle de render. Crearlos una vez.
- `Graphics2D.setRenderingHint()` solo cuando cambia el modo (no cada frame).
- Dibujar solo los tiles visibles: calcular rango de celdas visibles a partir de la cámara.

### Depuración
- `DebugRenderer`: clase separada que pinta sobre el render normal información como FPS, UPS, posición del jugador, grid de colisiones, radio de enemigos.
- Activable mediante flag en `GameConfig` o tecla de debug.

### Configuración
- `GameConfig` se carga desde un fichero `.properties` o valores por defecto en código.
- Nunca cambiar resolución o FPS en mitad del juego sin reinicializar el engine.

### Pruebas
- No usar JUnit para probar el motor en fase inicial. Usar ejecución visual.
- En fases avanzadas, extraer lógica pura (colisiones, proyección, A*) a métodos estáticos o sin estado para poder testearlos con JUnit.
- `IsoProjection` es candidata perfecta a tests unitarios desde el inicio.

### Evolución del Diseño
- No extraer interfaces hasta que haya dos implementaciones reales. El sobrediseño temprano ralentiza.
- Refactorizar al final de cada fase, no durante.
- Si una clase supera las 300 líneas, es señal de que tiene demasiadas responsabilidades.

---

## 11. Estrategia de Documentación

### Documentación en Código
- **Javadoc obligatorio** en toda clase pública: `@author`, descripción del propósito, `@since` con versión de fase.
- **Javadoc en métodos públicos**: qué hace, parámetros, retorno, excepciones posibles.
- **Comentarios inline** solo cuando la lógica no es obvia (matemáticas de proyección, A*, orden de pintado).
- Prohibido comentar qué hace el código cuando el nombre del método ya lo dice.

### Archivos Markdown desde el Inicio

| Fichero | Contenido |
|---|---|
| `README.md` | Descripción del proyecto, cómo compilar, cómo ejecutar, estado actual |
| `doc/ARCHITECTURE.md` | Arquitectura general, diagrama de módulos, decisiones técnicas clave |
| `doc/ROADMAP.md` | Fases de desarrollo, estado de cada fase (✅/🔄/⏳), próximos pasos |
| `doc/MODULES.md` | Descripción de cada módulo: responsabilidad, clases, dependencias |
| `doc/PROJECTION.md` | Matemáticas de proyección diamétrica, fórmulas, ejemplos |
| `doc/MAP_FORMAT.md` | Formato de los mapas, capas, tipos de tile, cómo definir eventos |
| `doc/phases/PHASE_01.md` | Objetivo, clases creadas, decisiones tomadas, resultado de la fase 1 |
| `doc/phases/PHASE_0N.md` | Ídem para cada fase |

### Actualización por Fase
- Al iniciar cada fase: crear `PHASE_0N.md` con objetivo y plan.
- Al finalizar cada fase: actualizar `ROADMAP.md` (marcar como ✅), actualizar `README.md` si cambia cómo compilar/ejecutar, completar `PHASE_0N.md` con lo que realmente se implementó.
- Si se modifica una decisión técnica: actualizar `ARCHITECTURE.md` inmediatamente.

### Sincronización Documentación/Código
- La regla es: **si cambias una clase pública, actualizas su Javadoc en el mismo commit**.
- Los `.md` de módulos se actualizan cuando cambia la API o las dependencias de ese módulo.
- `ROADMAP.md` es el documento vivo del estado del proyecto.

### Nivel de Detalle en Comentarios
- **Clases**: descripción completa de propósito, responsabilidad y relaciones.
- **Métodos públicos**: siempre documentados.
- **Métodos privados**: documentados si la lógica no es trivial.
- **Constantes y campos**: descripción si el nombre no es suficientemente claro.
- **Matemáticas no triviales**: siempre comentadas con la fórmula y referencia.

---

## 12. Primera Iteración Lista para Implementar

### Objetivo
Tener una ventana Swing abierta con un game loop funcional que pinte un mapa diamétrico 5×5 hardcodeado y un cuadrado/rombo representando al jugador en la celda central. Compilable y ejecutable.

### Clases a Crear (en orden)

| Orden | Clase | Responsabilidad |
|---|---|---|
| 1 | `GameConfig` | Resolución (800×600), tileW=64, tileH=32, FPS=60, UPS=60 |
| 2 | `IsoProjection` | `worldToScreen(col,row,z)` → ScreenPoint. Fórmula 2:1 |
| 3 | `WorldPoint` | Record/clase simple: float col, row, z |
| 4 | `ScreenPoint` | Record/clase simple: int x, y |
| 5 | `TileType` | Enum: GRASS, WATER, WALL con color y walkable |
| 6 | `Tile` | TileType + boolean walkable |
| 7 | `TileLayer` | Array 2D de Tile, getter por (col,row) |
| 8 | `TileMap` | Contiene TileLayer visual, dimensiones, crea mapa hardcodeado |
| 9 | `Camera` | offsetX, offsetY calculados desde posición jugador. Sin límites aún |
| 10 | `Entity` | Clase abstracta: WorldPoint position, float speed |
| 11 | `Player` | Extiende Entity. Posición fija en centro del mapa en fase 1 |
| 12 | `TileRenderer` | Dibuja rombo de cada tile con color de TileType |
| 13 | `EntityRenderer` | Dibuja rombo pequeño en posición del jugador |
| 14 | `GameRenderer` | Llama a TileRenderer y EntityRenderer con orden correcto |
| 15 | `GameWindow` | JFrame + JPanel con BufferStrategy |
| 16 | `GameLoop` | Hilo con UPS/FPS desacoplados, llama update() y render() |
| 17 | `GameEngine` | Orquesta todo: crea mapa, jugador, renderer, arranca loop |
| 18 | `Application` | `main()`: crea GameEngine y llama a `start()` |

### Resultado Esperado
Al ejecutar `Application.main()`:
- Se abre una ventana 800×600.
- Se ve un mapa diamétrico 5×5 con tiles de colores distintos.
- En el centro del mapa hay un rombo de color diferente (el jugador).
- El loop corre a 60 UPS / 60 FPS.
- En consola se imprime FPS y UPS cada segundo (debug básico).

### Documentación en Código
- Javadoc completo en las 18 clases.
- Comentario con la fórmula de proyección en `IsoProjection`.
- Comentario explicando el orden de pintado en `TileRenderer`.

### Documentos `.md` a Crear
- `README.md`: descripción, cómo compilar, cómo ejecutar.
- `doc/ARCHITECTURE.md`: diagrama de módulos y decisiones técnicas de fase 1.
- `doc/ROADMAP.md`: fases listadas, fase 1 en progreso.
- `doc/PROJECTION.md`: fórmulas de proyección con ejemplo numérico.
- `doc/phases/PHASE_01.md`: objetivo, clases creadas, resultado.

---

## 13. Prompt de Continuación para Implementación

```
Implementa la Fase 1 del Diametric Game Engine en Java.

## Contexto del proyecto
- Proyecto Maven en Java 21, groupId: es.noa.rad, artifactId: diametric-game-engine
- Motor de juego 2D con proyección diamétrica 2:1
- Renderizado exclusivamente con Graphics2D (javax.swing + java.awt)
- Sin frameworks externos

## Estructura de paquetes base
es.noa.rad/
  Application.java
  core/ → GameEngine, GameLoop, GameWindow, GameState
  config/ → GameConfig
  projection/ → IsoProjection, WorldPoint, ScreenPoint
  map/ → TileMap, TileLayer, Tile, TileType
  camera/ → Camera
  entity/ → Entity, Player
  render/ → GameRenderer, TileRenderer, EntityRenderer

## Lo que debe implementarse en esta fase

### GameConfig
- Constantes: SCREEN_WIDTH=800, SCREEN_HEIGHT=600, TILE_WIDTH=64, TILE_HEIGHT=32, TARGET_FPS=60, TARGET_UPS=60

### IsoProjection
- worldToScreen(float col, float row, float z): ScreenPoint
  → screenX = (col - row) * (TILE_WIDTH / 2)
  → screenY = (col + row) * (TILE_HEIGHT / 2) - z * TILE_HEIGHT
- screenToWorld(int sx, int sy): WorldPoint (z=0)

### WorldPoint y ScreenPoint
- Clases simples con campos públicos finales o getters

### TileType (enum)
- GRASS (verde, walkable=true), WATER (azul, walkable=false), WALL (gris, walkable=false)
- Cada valor tiene: Color color, boolean walkable

### Tile
- TileType type, getter isWalkable()

### TileLayer
- Tile[][] tiles, int width, int depth
- getTile(int col, int row): Tile

### TileMap
- TileLayer visualLayer, int width, int depth
- Mapa hardcodeado 7x7: borde de WALL, interior de GRASS, una celda de WATER en (3,3)
- getVisualLayer(): TileLayer

### Entity (abstracta)
- WorldPoint position, float speed
- abstract void update(double deltaTime)

### Player
- Extiende Entity
- Posición inicial: col=3, row=3, z=0
- update() vacío en fase 1

### Camera
- float offsetX, offsetY
- update(WorldPoint target, IsoProjection proj): calcula offset para centrar target en pantalla
- apply(Graphics2D g): translate con el offset

### TileRenderer
- render(Graphics2D g, TileMap map, Camera camera):
  - Iterar por row de 0 a depth, por col de 0 a width
  - Para cada tile: calcular screenPos con IsoProjection
  - Dibujar polígono rombo con color de TileType
  - Orden: row exterior, col interior (painter's algorithm básico)

### EntityRenderer
- render(Graphics2D g, Entity entity, Camera camera):
  - Convertir posición a pantalla
  - Dibujar rombo pequeño (20x10) en color rojo

### GameRenderer
- render(Graphics2D g, TileMap map, Player player, Camera camera):
  - Llamar TileRenderer.render()
  - Llamar EntityRenderer.render() para el jugador
  - Mostrar FPS en esquina superior izquierda

### GameWindow
- JFrame con título "Diametric Game Engine"
- JPanel con BufferStrategy de 2 buffers
- Método getGraphics2D(): Graphics2D del buffer activo
- Método show(): hace show del buffer

### GameState (enum)
- LOADING, RUNNING, PAUSED, STOPPED

### GameLoop
- Hilo propio (implements Runnable)
- Loop con UPS y FPS desacoplados usando System.nanoTime()
- Llama engine.update(deltaTime) y engine.render() según corresponda
- Imprime FPS y UPS reales en consola cada segundo

### GameEngine
- Crea: GameConfig, TileMap, Player, Camera, GameRenderer, GameWindow
- start(): inicializa todo y arranca GameLoop
- update(double deltaTime): actualiza Camera
- render(): obtiene Graphics2D de GameWindow, llama GameRenderer, hace show

### Application
- main(): new GameEngine().start()

## Requisitos obligatorios
- El resultado debe compilar y ejecutar sin errores
- Al ejecutar, debe abrirse una ventana 800x600 con el mapa diamétrico y el jugador visible
- Javadoc completo en todas las clases y métodos públicos
- Comentario con la fórmula de proyección en IsoProjection
- Comentario explicando el orden de pintado en TileRenderer

## Documentación Markdown a generar
Crea o actualiza los siguientes ficheros en doc/:

### README.md (en raíz del proyecto)
- Descripción del proyecto
- Requisitos (Java 21, Maven)
- Cómo compilar: comando Maven con JDK y Maven del TCN-IDE
- Cómo ejecutar: comando java -jar

### doc/ARCHITECTURE.md
- Diagrama de módulos (texto ASCII)
- Decisiones técnicas de fase 1: proyección, orden de pintado, separación lógica/render

### doc/ROADMAP.md
- Lista de las 7 fases
- Fase 1: 🔄 En progreso

### doc/PROJECTION.md
- Fórmulas de proyección worldToScreen y screenToWorld
- Ejemplo numérico para col=3, row=3, z=0 con tileWidth=64, tileHeight=32

### doc/phases/PHASE_01.md
- Objetivo de la fase
- Clases implementadas
- Decisiones tomadas
- Resultado obtenido

## Orden de implementación recomendado
1. GameConfig → 2. WorldPoint, ScreenPoint → 3. IsoProjection → 4. TileType, Tile, TileLayer, TileMap → 5. Entity, Player → 6. Camera → 7. TileRenderer, EntityRenderer, GameRenderer → 8. GameWindow, GameState → 9. GameLoop → 10. GameEngine → 11. Application → 12. Ficheros .md
```

```
🎮 Diametric Game Engine — Fase 2: Input y Movimiento del Jugador
Contexto del proyecto
Motor de juego isométrico diametric 2:1 en Java puro (Graphics2D + Swing), sin frameworks externos.

JDK: Temurin 21 en Temurin-21.0.9+10
Maven: mvn.cmd
Proyecto: C:\Users\x922180\Copilot\Diametric Game Engine
pom.xml: groupId es.noa.rad, mainClass es.noa.rad.Application, jar final Diametric Game Engine-0.0.0.jar
Proyección: screenX = (col - row) * 32, screenY = (col + row) * 16 - z * 32
Estado actual (Fase 1 completada ✅)
Paquetes existentes bajo rad:

El bucle corre estable a 60 FPS / 60 UPS. La ventana 800×600 muestra el mapa y el marcador rojo del jugador.

Objetivo de la Fase 2
Añadir input por teclado y movimiento del jugador en coordenadas de mundo, con detección de colisión contra tiles no caminables.

Arquitectura a implementar
1. input/InputState.java
Clase singleton o pasada por inyección, sin Swing en el bucle.
boolean[] keys indexado por KeyEvent.VK_*.
Métodos: keyPressed(int code), keyReleased(int code), isDown(int code).
Implementa KeyListener para registrarse en el Canvas de GameWindow.
2. input/InputHandler.java
Implementa KeyListener, delega en InputState.
Registrado en GameWindow durante GameEngine.start().
3. Modificar entity/Player.java
Recibir InputState e IsoProjection en el constructor o método update.
Direcciones diametricas del teclado:
W / ↑ → col-- (noroeste isométrico)
S / ↓ → col++ (sureste)
A / ← → row-- (noreste)
D / → → row++ (suroeste)
Velocidad en unidades de tile por segundo usando deltaTime.
Colisión: comprobar TileMap.getVisualLayer().getTile(newCol, newRow).isWalkable() antes de aplicar el movimiento.
4. Modificar core/GameEngine.java
Instanciar InputState.
Pasar InputState y TileMap al método update de Player.
Registrar InputHandler en GameWindow.
5. Modificar render/GameRenderer.java (HUD ampliado)
Añadir al HUD: posición del jugador [col, row] en tiempo real.
Formato: "FPS: xx  UPS: xx  POS: [3.0, 3.0]"
Clases nuevas / modificadas
Archivo	Acción
input/InputState.java	NUEVA
input/InputHandler.java	NUEVA
entity/Player.java	MODIFICAR — añadir lógica de movimiento y colisión
core/GameEngine.java	MODIFICAR — instanciar input, conectar con player
render/GameRenderer.java	MODIFICAR — HUD con posición
doc/phases/PHASE_02.md	NUEVA
Actualizar ROADMAP.md	Fase 2 marcada 🔄
Orden de implementación recomendado
input/InputState.java
input/InputHandler.java
entity/Player.java (añadir movimiento + colisión)
core/GameEngine.java (conectar input)
render/GameRenderer.java (HUD posición)
doc/phases/PHASE_02.md
Actualizar ROADMAP.md
Compilar y ejecutar — mover jugador con WASD, verificar que no atraviesa muros ni agua
Criterios de éxito
 El jugador se mueve con WASD a velocidad constante (~3 tiles/s) independiente del FPS.
 El jugador no puede entrar en tiles WALL ni WATER.
 La cámara sigue al jugador suavemente.
 El HUD muestra posición en tiempo real.
 mvn clean package produce BUILD SUCCESS sin warnings.
Comandos de build/run
Implementa la Fase 2 del Diametric Game Engine en Java siguiendo el orden de implementación indicado. Cada clase debe incluir Javadoc. Al finalizar, compila y ejecuta para verificar el movimiento del jugador con WASD y la detección de colisión.
```
