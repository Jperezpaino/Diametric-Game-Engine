# UI-002 - Structure Builder

## 1. Objetivo

Structure Builder es la herramienta responsable de la creación, modificación y almacenamiento de Structures.

Una Structure es una agrupación reutilizable de Tiles organizadas espacialmente mediante coordenadas relativas X,Y,Z.

La aplicación permite construir elementos complejos que posteriormente podrán ser utilizados dentro de los mapas del juego mediante Map Builder.

Ejemplos de Structures:

* Columnas
* Árboles
* Casas
* Escaleras
* Puentes
* Muros
* Decoraciones complejas

La aplicación genera como resultado ficheros de tipo Structure.

---

# 2. Ventana Principal

La aplicación estará compuesta por una única ventana principal de trabajo.

Características:

| Propiedad       | Valor             |
| --------------- | ----------------- |
| Título          | Structure Builder |
| Tamaño mínimo   | 640 x 480         |
| Redimensionable | Sí                |
| Maximizable     | Sí                |
| Minimizable     | Sí                |
| Barra de estado | Sí                |

Cuando no exista ningún fichero abierto se mostrará una pantalla de bienvenida con una imagen conceptual representativa de la aplicación.

La imagen deberá transmitir visualmente la construcción de estructuras mediante Tiles.

---

# 3. Distribución General

La ventana principal estará organizada en cinco áreas principales.

```text
┌──────────────────────────────────────────────────────────────┐
│ Menu Bar                                                     │
├──────────────────────────────────────────────────────────────┤
│ Toolbar                                                      │
├───────────────┬───────────────────────┬──────────────────────┤
│               │                       │                      │
│ Tile Library  │ Structure Viewport    │ Structure Properties │
│               │                       │                      │
│               │                       │                      │
│               │                       │                      │
├───────────────┴───────────────────────┴──────────────────────┤
│ Status Bar                                                   │
└──────────────────────────────────────────────────────────────┘
```

---

# 4. Componentes Principales

## 4.1 Menu Bar

Responsabilidad:

Proporcionar acceso a todas las operaciones globales de la aplicación.

---

### Archivo (File)

#### Nuevo fichero de Structures

Atajo:

```text
Ctrl + N
```

Acción:

* Crea una nueva definición de Structures.
* Descarta los cambios no guardados previa confirmación.

---

#### Abrir fichero de Structures

Atajo:

```text
Ctrl + O
```

Acción:

* Permite seleccionar un fichero de Structures existente.
* Carga todas las Structures definidas.

---

#### Guardar fichero de Structures

Atajo:

```text
Ctrl + S
```

Acción:

* Guarda el fichero actual.

---

#### Guardar como

Acción:

* Guarda el proyecto con un nuevo nombre.

---

#### Cerrar

Atajo:

```text
Ctrl + W
```

Acción:

* Cierra el fichero actual.

---

#### Salir

Atajo:

```text
Alt + F4
```

Acción:

* Finaliza la aplicación.

---

### Recursos (Resources)

Responsabilidad:

Gestionar el acceso a las bibliotecas de Tiles.

Opciones mínimas:

```text
Resources
 ├─ Open Tile Library
 ├─ Reload Tile Library
 └─ Tile Library Information
```

---

### Herramientas (Tools)

Responsabilidad:

Controlar la edición de Structures.

Opciones mínimas:

```text
Tools
 ├─ Show Grid
 ├─ Show Coordinates
 ├─ Show Origin
 └─ Recalculate Structure Information
```

---

### Ayuda (Help)

Opciones:

```text
Help
 └─ About Structure Builder
```

Atajo:

```text
F1
```

---

# 4.2 Toolbar

Responsabilidad:

Acceso rápido a las herramientas de edición.

Herramientas mínimas:

```text
New
Open
Save

Undo
Redo

Select Tile
Place Tile
Delete Tile

Move Layer Up
Move Layer Down

Show Grid
Show Origin
```

Cada herramienta deberá disponer de:

* Icono
* Texto descriptivo
* Tooltip

---

# 4.3 Tile Library

Ubicación:

Panel izquierdo.

Responsabilidad:

Mostrar todas las Tiles disponibles para construir Structures.

Contenido:

```text
Terrain
 ├─ Grass_Flat
 ├─ Stone_Flat

Walls
 ├─ Stone_Wall
 ├─ Brick_Wall

Special
 ├─ Column_Top
 ├─ Column_Mid
 └─ Column_Base
```

Funciones:

* Buscar Tiles.
* Filtrar Tiles.
* Seleccionar Tiles.
* Mostrar vista previa.

La biblioteca deberá cargarse desde los ficheros generados por Tile Builder.

---

# 4.4 Tile Preview

Ubicación:

Parte inferior del panel izquierdo.

Responsabilidad:

Mostrar información de la Tile seleccionada.

Información mínima:

```text
Nombre
Material
Shape
Vista previa gráfica
```

---

# 4.5 Structure Viewport

Ubicación:

Zona central.

Responsabilidad:

Área principal de construcción de Structures.

La vista representa un espacio tridimensional compuesto por coordenadas relativas X,Y,Z.

Todas las Tiles colocadas dentro del Viewport pertenecerán a la Structure actualmente seleccionada.

Funciones mínimas:

* Colocar Tiles.
* Eliminar Tiles.
* Seleccionar Tiles.
* Navegar por la estructura.
* Visualizar coordenadas.
* Visualizar origen.
* Visualizar niveles Z.

---

## Sistema de Coordenadas

La vista deberá representar:

```text
X = ancho
Y = largo
Z = altura
```

---

## Origen de la Structure

Toda Structure deberá visualizar claramente su origen.

Definición:

```text
Origin = (0,0,0)
```

El origen representa el punto de anclaje utilizado posteriormente por Map Builder.

---

## Rejilla de Edición

La vista deberá mostrar una rejilla de referencia.

Funciones:

* Facilitar alineación.
* Mostrar coordenadas.
* Posicionar Tiles.

La visibilidad de la rejilla podrá activarse o desactivarse.

---

# 4.6 Selector de Capas (Z)

Ubicación:

Integrado dentro del Viewport.

Responsabilidad:

Permitir navegar entre niveles de altura.

Información mínima:

```text
Nivel actual
Número total de niveles
```

Funciones:

* Subir nivel.
* Bajar nivel.
* Seleccionar nivel concreto.

---

# 4.7 Structure Properties

Ubicación:

Panel derecho.

Responsabilidad:

Mostrar y modificar la información general de la Structure.

---

## Nombre

Tipo:

Texto.

Ejemplo:

```text
Stone_Column
```

---

## Descripción

Tipo:

Texto multilínea.

Ejemplo:

```text
Columna de piedra utilizada en edificios.
```

---

## Origen

Información mostrada:

```text
Origin X
Origin Y
Origin Z
```

Inicialmente:

```text
0
0
0
```

---

## Tamaño

Información calculada automáticamente.

```text
Width (X)
Depth (Y)
Height (Z)
```

---

## Tiles Utilizadas

Información:

```text
Número total de Tiles
Número de tipos distintos de Tile
```

---

## Recalcular Información

Botón encargado de actualizar:

* Dimensiones.
* Estadísticas.
* Referencias.

---

# 4.8 Información del Tile Seleccionado

Ubicación:

Parte inferior del panel derecho.

Responsabilidad:

Mostrar información de la Tile seleccionada dentro del Viewport.

Información mínima:

```text
Nombre
Material
Shape
Coordenadas relativas
```

---

# 4.9 Status Bar

Ubicación:

Parte inferior de la ventana.

Responsabilidad:

Mostrar información contextual.

Información mínima:

```text
Structure actual
Número de Tiles
Dimensiones
Origen
Estado de guardado
```

Ejemplo:

```text
Stone_Column | 27 Tiles | 1x1x9 | Origin(0,0,0) | Saved
```

---

# 5. Flujo de Trabajo

Flujo principal esperado:

```text
Abrir biblioteca de Tiles
        ↓
Crear nueva Structure
        ↓
Seleccionar Tile
        ↓
Colocar Tile en coordenadas X,Y,Z
        ↓
Construir Structure
        ↓
Validar información
        ↓
Guardar fichero de Structures
```

---

# 6. Restricciones

* Toda Structure deberá contener al menos una Tile.
* Las coordenadas serán relativas al origen.
* El origen siempre será (0,0,0).
* No se permitirá guardar Structures vacías.
* Los cambios no guardados deberán detectarse automáticamente.
* Toda Tile utilizada deberá existir en la biblioteca de Tiles.

---

# 7. Compatibilidad Futura

La interfaz deberá diseñarse permitiendo futuras ampliaciones:

* Rotación de Structures.
* Variantes de Structures.
* Prefabs complejos.
* Importación masiva de Structures.
* Herramientas avanzadas de selección.
* Herramientas de copia y pegado.
* Simetría automática.
* Integración directa con Map Builder.

Estas ampliaciones no deberán requerir cambios estructurales significativos en la interfaz principal.
