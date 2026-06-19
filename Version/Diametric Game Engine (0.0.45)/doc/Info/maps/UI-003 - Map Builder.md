# UI-003 - Map Builder

## 1. Objetivo

Map Builder es la herramienta responsable de la creación, modificación y almacenamiento de mapas completos del juego.

La aplicación permite construir escenarios mediante la colocación de Structures y Tiles previamente definidas en coordenadas absolutas del mapa.

El resultado final son ficheros de tipo Map que serán utilizados directamente por el motor del juego.

La herramienta deberá permitir:

* Crear mapas.
* Modificar mapas.
* Navegar por mapas de gran tamaño.
* Colocar Structures.
* Colocar Tiles individuales.
* Gestionar alturas (eje Z).
* Visualizar el resultado final del escenario.

---

# 2. Ventana Principal

La aplicación estará compuesta por una única ventana principal de trabajo.

Características:

| Propiedad       | Valor       |
| --------------- | ----------- |
| Título          | Map Builder |
| Tamaño mínimo   | 640 x 480   |
| Redimensionable | Sí          |
| Maximizable     | Sí          |
| Minimizable     | Sí          |
| Barra de estado | Sí          |

Cuando no exista ningún mapa abierto se mostrará una pantalla de bienvenida con una imagen conceptual representativa de la construcción de mapas mediante Structures y Tiles.

---

# 3. Distribución General

La ventana principal estará organizada en tres columnas principales.

```text
┌────────────────────────────────────────────────────────────────────────────┐
│ Menu Bar                                                                   │
├────────────────────────────────────────────────────────────────────────────┤
│ Toolbar                                                                    │
├──────────────┬──────────────────────────────────────┬──────────────────────┤
│              │                                      │                      │
│ Resource     │                                      │                      │
│ Browser      │           Map Viewport              │    Tool Panel        │
│              │                                      │                      │
│ Structures   │                                      │    Map Navigator    │
│ Tiles        │                                      │                      │
│              │                                      │    Tool Options     │
│              │                                      │                      │
│              │                                      │    Layer Controls   │
│              │                                      │                      │
├──────────────┴──────────────────────────────────────┴──────────────────────┤
│ Status Bar                                                                 │
└────────────────────────────────────────────────────────────────────────────┘
```

---

# 4. Componentes Principales

## 4.1 Menu Bar

Responsabilidad:

Proporcionar acceso a todas las operaciones globales de la aplicación.

---

### Archivo (File)

#### Nuevo fichero de Mapas

Atajo:

```text
Ctrl + N
```

Acción:

* Crear un nuevo mapa.
* Solicitar tamaño inicial del mapa.

---

#### Abrir fichero de Mapas

Atajo:

```text
Ctrl + O
```

Acción:

* Cargar un mapa existente.

---

#### Guardar fichero de Mapas

Atajo:

```text
Ctrl + S
```

Acción:

* Guardar el mapa actual.

---

#### Guardar como

Acción:

* Guardar con otro nombre.

---

#### Propiedades del mapa

Acción:

* Modificar información general del mapa.

---

#### Cerrar

Atajo:

```text
Ctrl + W
```

---

#### Salir

Atajo:

```text
Alt + F4
```

---

### Recursos (Resources)

Responsabilidad:

Gestionar las bibliotecas disponibles.

Opciones mínimas:

```text
Resources
 ├─ Open Structure Library
 ├─ Reload Structure Library
 ├─ Open Tile Library
 └─ Reload Tile Library
```

---

### Ver (View)

Responsabilidad:

Controlar la visualización.

Opciones mínimas:

```text
View
 ├─ Show Grid
 ├─ Show Coordinates
 ├─ Show Origin
 ├─ Show Layers
 └─ Show Map Navigator
```

---

### Herramientas (Tools)

Responsabilidad:

Funciones auxiliares del editor.

Opciones mínimas:

```text
Tools
 ├─ Recalculate Map
 ├─ Validate Map
 └─ Statistics
```

---

### Ayuda (Help)

Opciones:

```text
Help
 └─ About Map Builder
```

Atajo:

```text
F1
```

---

# 4.2 Toolbar

Responsabilidad:

Acceso rápido a las herramientas más utilizadas.

Herramientas mínimas:

```text
New
Open
Save

Undo
Redo

View
Edit
Select

Layer Up
Layer Down

Show Grid
Show Layers
```

Cada herramienta dispondrá de:

* Icono.
* Texto descriptivo.
* Tooltip.

---

# 4.3 Resource Browser

Ubicación:

Columna izquierda.

Responsabilidad:

Mostrar todos los recursos disponibles para la construcción del mapa.

---

## Pestaña Structures

Contenido:

```text
Structures File

castle.structures
nature.structures

------------------

Castle
Bridge
Tree
House
Column
Well
```

Funciones:

* Seleccionar fichero de Structures.
* Seleccionar Structure activa.
* Visualizar información básica.

---

## Pestaña Tiles

Contenido:

```text
Roof_Top
Roof_Mid
Wall_Stone
Wall_Window
Door_Wood
```

Las Tiles mostradas corresponderán a la Structure actualmente seleccionada.

Funciones:

* Seleccionar Tile individual.
* Visualizar información básica.

---

# 4.4 Map Viewport

Ubicación:

Zona central.

Responsabilidad:

Área principal de construcción del mapa.

La vista representa el escenario completo mediante coordenadas absolutas.

Funciones:

* Colocar Structures.
* Colocar Tiles.
* Eliminar elementos.
* Navegar por el mapa.
* Visualizar alturas.
* Visualizar coordenadas.
* Mostrar origen.

---

## Sistema de Coordenadas

El mapa utiliza:

```text
X = ancho
Y = largo
Z = altura
```

---

## Rejilla de Edición

La vista deberá mostrar una rejilla de referencia.

Funciones:

* Facilitar alineación.
* Facilitar posicionamiento.
* Mostrar coordenadas.

---

## Herramienta Vista

Modo destinado exclusivamente a navegación.

Permite:

* Desplazamiento.
* Zoom.
* Rotación futura.

No permite edición.

---

## Herramienta Edición

Modo destinado a construcción.

Permite:

* Colocar Structures.
* Colocar Tiles.
* Eliminar elementos.

La altura utilizada será la definida en el control de altura activo.

---

## Herramienta Selección

Primera versión:

* Selección visual.

Versiones futuras:

* Copiar.
* Mover.
* Eliminar.
* Duplicar.

---

# 4.5 Map Navigator

Ubicación:

Parte superior del panel derecho.

Responsabilidad:

Mostrar una representación reducida del mapa completo.

Funciones:

* Visualizar el área visible actual.
* Navegar rápidamente.
* Localizar zonas alejadas.

Cuando el mapa sea mayor que el área visible, deberá mostrarse un rectángulo indicando el área actualmente visible.

El usuario podrá desplazarse haciendo clic sobre cualquier zona del minimapa.

---

# 4.6 Tool Options

Ubicación:

Parte central del panel derecho.

Responsabilidad:

Mostrar las opciones asociadas a la herramienta activa.

---

## Modo Vista

Opciones:

```text
Zoom
Grid
Coordinates
```

---

## Modo Edición

Opciones:

### Recurso activo

```text
Structure
Tile
```

### Structure seleccionada

```text
Castle
Bridge
Tree
...
```

### Tile seleccionada

```text
Roof_Top
Wall_Stone
...
```

### Altura activa

```text
Z = 0
```

Controles:

```text
▲
▼
```

La rueda del ratón podrá modificar la altura activa.

---

## Información contextual

Mostrará:

```text
Elemento bajo cursor
X
Y
Z
```

---

# 4.7 Layer Controls

Ubicación:

Parte inferior del panel derecho.

Responsabilidad:

Gestionar la visualización por alturas.

---

## Activar visualización por capas

Control:

```text
[ ] Enable Layer View
```

---

## Capa actual

Control:

```text
Current Layer (Z)

▲
0
▼
```

---

## Mostrar sólo la capa actual

Control:

```text
[X]
```

Funciones:

* Facilitar construcción de edificios.
* Facilitar edición de niveles superiores.
* Reducir complejidad visual.

---

# 4.8 Status Bar

Ubicación:

Parte inferior de la ventana.

Responsabilidad:

Mostrar información contextual.

Información mínima:

```text
Mapa actual
Tamaño
Número de Structures
Número de Tiles
Posición del cursor
Altura actual
Estado de guardado
```

Ejemplo:

```text
DemoMap.map | 64x64 | Structures: 120 | Tiles: 1840 | X:18 Y:12 Z:0 | Saved
```

---

## 5. Ventanas Secundarias

Map Builder utilizará ventanas auxiliares para la configuración y validación de mapas.

Las ventanas secundarias forman parte del flujo normal de trabajo del editor y deberán mantener la misma apariencia visual que la ventana principal.

---

## 5.1 New Map Wizard

### Objetivo

Permitir la creación guiada de un nuevo mapa proporcionando toda la información necesaria para inicializar correctamente el proyecto.

Esta ventana deberá mostrarse automáticamente al ejecutar:

```text
File
 └─ New Map
```

o mediante:

```text
Ctrl + N
```

---

### Distribución General

```text
┌──────────────────────────────────────────────────────────────┐
│ New Map                                                      │
├──────────────────────────────┬───────────────────────────────┤
│                              │                               │
│ Map Properties              │ Structure Libraries           │
│                              │                               │
│ Map Name                    │ village.structures           │
│ Width (X)                   │ nature.structures            │
│ Height (Y)                  │ decorations.structures       │
│ Maximum Height (Z)          │                               │
│                              │ [Add] [Remove]              │
├──────────────────────────────┼───────────────────────────────┤
│ Template                    │ Map Preview                  │
│                              │                               │
│ Empty Map                   │ Isometric Preview            │
│ Village                     │                               │
│ Forest                      │                               │
│ Dungeon                     │                               │
├──────────────────────────────┴───────────────────────────────┤
│ Cancel                                      Create Map       │
└──────────────────────────────────────────────────────────────┘
```

---

### Campos

#### Map Name

Tipo:

```text
String
```

Descripción:

Nombre interno utilizado para identificar el mapa.

Campo obligatorio.

---

#### Width (X)

Tipo:

```text
Integer
```

Descripción:

Número de Tiles del mapa en el eje X.

Campo obligatorio.

---

#### Height (Y)

Tipo:

```text
Integer
```

Descripción:

Número de Tiles del mapa en el eje Y.

Campo obligatorio.

---

#### Maximum Height (Z)

Tipo:

```text
Integer
```

Descripción:

Altura máxima editable del mapa.

Campo obligatorio.

---

### Valores recomendados

#### Tamaño del mapa

```text
Small
32 x 32

Medium
64 x 64

Large
128 x 128

Very Large
256 x 256
```

---

#### Altura máxima (Z)

```text
Small Project
8

Medium Project
16

Large Project
32

Very Large Project
64
```

---

### Structure Libraries

Lista de bibliotecas de Structures disponibles desde el inicio.

Funciones:

```text
Add Library
Remove Library
Move Up
Move Down
Reload
```

Ejemplos:

```text
village.structures
nature.structures
castle.structures
```

---

### Template

Permite seleccionar una plantilla inicial.

Versión inicial:

```text
Empty Map
```

Plantillas reservadas para futuras versiones:

```text
Village
Forest
Dungeon
Castle
Mountain
```

---

### Map Preview

Responsabilidad:

Mostrar una representación simplificada de la configuración seleccionada.

Información mínima:

```text
Map Size
Maximum Height
Number of Libraries
Preview Layer
```

La vista previa será informativa y no editable.

---

### Validation Rules

Antes de permitir la creación del mapa se deberán validar las siguientes condiciones.

#### Map Name

```text
Required
```

---

#### Width

```text
Greater than 0
```

---

#### Height

```text
Greater than 0
```

---

#### Maximum Height

```text
Greater than 0
```

---

#### Structure Libraries

```text
At least one library required
```

---

### Initial View Settings

Configuración visual inicial del editor.

Opciones:

```text
[X] Show Grid

[X] Show Coordinates

[X] Enable Layer View
```

Todos los valores estarán activados por defecto.

---

### Acciones automáticas al pulsar Create Map

El sistema deberá:

```text
1. Crear la definición de Map.
2. Inicializar las dimensiones definidas.
3. Crear la capa Z = 0.
4. Cargar todas las bibliotecas seleccionadas.
5. Inicializar el Resource Browser.
6. Inicializar el Map Navigator.
7. Centrar el Viewport.
8. Activar la herramienta View.
9. Posicionar el cursor en el origen.
10. Actualizar la barra de estado.
```

---

## 5.2 Map Properties

### Objetivo

Modificar la información general de un mapa ya existente.

Accesible desde:

```text
File
 └─ Map Properties
```

---

### Campos

```text
Map Name

Description

Map Width

Map Height

Maximum Height (Z)
```

---

### Restricciones

Las modificaciones deberán validarse antes de aplicarse.

El sistema deberá advertir al usuario si una reducción del tamaño del mapa implica pérdida de información.

---

## 5.3 Validation Report

### Objetivo

Mostrar errores y advertencias detectados en el mapa actual.

---

### Ejemplos de errores

```text
Structure not found

Tile reference invalid

Coordinates outside map bounds

Invalid Z coordinate

Missing library
```

---

### Ejemplos de advertencias

```text
Unused structures

Unused tiles

Large map dimensions

Excessive maximum height
```

---

# 9. Futuras Mejoras

Las siguientes funcionalidades no forman parte de la primera versión pero deberán quedar contempladas dentro del diseño general de la aplicación.

---

## 9.1 F-001 Plantillas de Mapas

Permitir generar mapas base a partir de configuraciones predefinidas.

Ejemplos:

```text
Village
Forest
Dungeon
Castle
Mountain
```

---

## 9.2 F-002 Multi-Biblioteca

Permitir utilizar simultáneamente múltiples bibliotecas de Structures y Tiles.

Ejemplo:

```text
nature.structures
village.structures
dungeon.structures
```

---

## 9.3 F-003 Sistema de Layers

Separación lógica del contenido.

Ejemplos:

```text
GROUND
OBJECT
DECORATION
EFFECT
COLLISION
```

Independiente del sistema Z.

---

## 9.4 F-004 Auto-Tiling

Selección automática de Tiles según los vecinos.

Casos de uso:

```text
Water
Roads
Walls
Fences
Cliffs
```

---

## 9.5 F-005 Herramientas Avanzadas de Selección

Extender el modo Select.

Funciones previstas:

```text
Copy
Paste
Move
Duplicate
Delete
```

---

## 9.6 F-006 Pinceles de Construcción

Permitir colocar múltiples elementos mediante herramientas de pintura.

Ejemplos:

```text
Brush
Rectangle
Circle
Line
Fill
```

---

## 9.7 F-007 Rotación de Structures

Permitir la colocación de Structures con distintas orientaciones.

Estado actual:

```text
No implementado
```

Dependencia:

Disponibilidad de recursos gráficos para cada orientación.

---

## 9.8 F-008 Generación Procedural

Herramientas de generación automática.

Ejemplos:

```text
Forests
Mountains
Rivers
Dungeons
Cities
```

---

## 9.9 F-009 Integración con el Motor

Permitir ejecutar el mapa directamente dentro del motor.

Ejemplos:

```text
Play Map
Test Map
Debug Map
```

---

## 9.10 F-010 Sistema de Plugins

Permitir ampliar la funcionalidad del editor sin modificar el núcleo.

Ejemplos:

```text
Importers
Exporters
Generators
Validators
```

---

## 9.11 F-011 Prefabs

### Objetivo

Permitir reutilizar conjuntos completos de Structures previamente configuradas.

Ejemplos:

```text
House + Well + Fence

Market Area

Road Crossing

Decorated Bridge

Village Block

Castle Entrance
```

Los Prefabs representan un nivel intermedio entre Structure y Map.

```text
Tile
    ↓
Structure
    ↓
Prefab
    ↓
Map
```

Esta funcionalidad deberá poder integrarse sin modificar la arquitectura principal del sistema.
