# UI-004.3 - Tile Group Editor

## 1. Objetivo

Tile Group Editor es una herramienta especializada de Pixel Builder destinada a la creación simultánea de múltiples Tiles conectadas visualmente.

Su finalidad es permitir al usuario diseñar conjuntos de Tiles relacionadas como una única composición gráfica, evitando tener que editar individualmente cada Tile y permitiendo visualizar el resultado final durante el proceso de creación.

Ejemplos de uso:

* Columnas
* Escaleras
* Puertas
* Ventanas
* Tejados
* Casas pequeñas
* Muros
* Elementos decorativos complejos

El editor trabaja sobre una composición temporal y distribuye posteriormente el resultado sobre las Tiles correspondientes.

---

# 2. Conceptos Fundamentales

## 2.1 Tile Group

Un Tile Group representa un conjunto de Tiles organizadas espacialmente mediante dimensiones:

```text
Width (X)
Depth (Y)
Height (Z)
```

Limitaciones iniciales:

```text
Width: 1..5
Depth: 1..5
Height: 1..10
```

---

## 2.2 Superficies Visibles

No todas las posiciones del grupo generan Tiles editables.

Las superficies completamente ocultas por otras superficies no forman parte de la composición gráfica.

Únicamente se consideran editables:

```text
Top Faces
Left Faces
Right Faces
```

Ejemplo:

```text
5 x 5 x 10
```

No genera:

```text
250 superficies editables
```

Sino únicamente las superficies visibles desde la vista isométrica utilizada por el editor.

---

## 2.3 Working Canvas

Durante la edición el usuario no modifica directamente las Tiles.

El sistema crea un lienzo temporal denominado:

```text
Working Canvas
```

El usuario trabaja sobre esta composición.

Al guardar:

```text
Working Canvas
    ↓
Distribución automática
    ↓
Tiles resultantes
```

---

# 3. Distribución General

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Tile Group Editor                                                    │
├──────────────────────────────────────────────────────────────────────┤
│ Toolbar                                                              │
├───────────────┬──────────────────────────────┬───────────────────────┤
│               │                              │                       │
│ Tile List     │     Working Canvas           │ Properties            │
│               │                              │                       │
│               │                              │                       │
│               │                              │                       │
├───────────────┴──────────────────────────────┴───────────────────────┤
│ Save                                        Cancel                   │
└──────────────────────────────────────────────────────────────────────┘
```

---

# 4. Componentes Principales

## 4.1 Toolbar

La barra de herramientas utilizará exactamente las mismas herramientas disponibles en Tile Editor.

### Herramientas de dibujo

```text
Pixel
Line
Rectangle
Fill
Color Picker
Eraser
```

---

### Herramientas de selección

```text
Rectangular Selection
Free Selection
Copy
Paste
```

---

### Herramientas de transformación

```text
Horizontal Flip
Vertical Flip
```

---

### Navegación

```text
Zoom In
Zoom Out
```

Rangos permitidos:

```text
0.25x
0.5x
1x
2x
4x
8x
10x
```

---

### Historial

```text
Undo
Redo
```

Operaciones registradas:

* Pintado de píxeles.
* Líneas.
* Rectángulos.
* Rellenos.
* Borrados.
* Pegados.
* Volteos.
* Reemplazos de Tiles.
* Cambios de metadatos.

---

# 4.2 Working Canvas

## Objetivo

Representar visualmente el conjunto completo de Tiles conectadas.

---

## Características

El lienzo mostrará:

```text
Proyección isométrica completa
```

No mostrará las Tiles individualmente separadas.

El usuario percibirá el grupo como un único objeto.

---

## Fondo Guía

El editor mostrará automáticamente:

```text
Límites del Tile Group
```

según:

```text
Width
Depth
Height
```

---

## Restricciones

No se permitirá pintar fuera de:

```text
Superficies visibles
```

Las superficies ocultas no serán editables.

---

## Coordenadas

Se mostrarán continuamente:

```text
X
Y
```

del píxel bajo el cursor.

---

## Cuadrícula

La cuadrícula podrá activarse o desactivarse.

```text
Show Grid
```

---

# 4.3 Real Size Preview

## Objetivo

Mostrar permanentemente el resultado final a escala real.

Responsabilidades:

* Verificar proporciones.
* Detectar errores visuales.
* Comprobar la legibilidad final.

La vista previa será exclusivamente informativa.

No será editable.

---

# 4.4 Tile List

## Objetivo

Mostrar todas las Tiles visibles generadas por el Tile Group.

Ejemplo:

```text
Roof_NW
Roof_NE

Wall_Left
Wall_Right

Door
```

---

## Información mostrada

Cada fila deberá mostrar:

```text
Tile Name
Face
Status
Source Tile
Modified
```

Ejemplo:

```text
Roof_NW

Face:
Top

Status:
Existing

Source:
roof_01

Modified:
Yes
```

---

## Operaciones permitidas

### Edit

Abre Tile Editor para la Tile seleccionada.

---

### Replace

Permite sustituir una Tile por otra existente.

Ejemplo:

```text
Roof_NW
    ↓
Roof_NW_Variant_02
```

---

### Duplicate

Duplica la Tile seleccionada.

---

### Create New

Genera una nueva Tile independiente.

---

### Remove

Elimina la referencia actual.

---

## Metadatos

Las Tiles nuevas deberán permitir modificar:

```text
Name
Palette
Description
Tags
```

---

# 4.5 Properties Panel

## Objetivo

Mostrar información general del Tile Group.

---

### Name

Nombre del grupo.

Ejemplo:

```text
Stone Column
```

---

### Width

```text
1..5
```

---

### Depth

```text
1..5
```

---

### Height

```text
1..10
```

---

### Visible Tiles

Calculado automáticamente.

---

### Modified Tiles

Calculado automáticamente.

---

# 5. Guardado

## Objetivo

Persistir el resultado del Working Canvas.

---

## Procedimiento

### Paso 1

Identificar Tiles modificadas.

```text
Tile_A
Tile_B
Tile_C
```

---

### Paso 2

Determinar si son Tiles existentes o nuevas.

---

### Paso 3

Para cada Tile modificada existente:

Mostrar diálogo:

```text
Tile modified

Modify Original

or

Create Duplicate
```

---

### Paso 4

Aplicar los cambios seleccionados.

---

### Paso 5

Actualizar el Tile Group.

---

# 6. Reglas de Negocio

## RB-001

Las superficies ocultas no generan Tiles.

---

## RB-002

No se permitirá pintar fuera de las superficies visibles.

---

## RB-003

Las modificaciones se realizarán siempre sobre Working Canvas.

---

## RB-004

Las Tiles originales no podrán modificarse silenciosamente.

Siempre deberá existir confirmación explícita.

---

## RB-005

Una Tile podrá pertenecer simultáneamente a múltiples Tile Groups.

---

## RB-006

Las Tiles duplicadas deberán recibir un identificador único.

---

# 7. Flujo de Trabajo

```text
Crear Tile Group
        ↓
Definir dimensiones
        ↓
Generar superficies visibles
        ↓
Asignar Tiles existentes
        ↓
Dibujar sobre Working Canvas
        ↓
Visualizar resultado
        ↓
Guardar
        ↓
Modificar o duplicar Tiles afectadas
        ↓
Actualizar Tile Group
```

---

# 8. Compatibilidad Futura

Las siguientes funcionalidades quedan reservadas para versiones posteriores.

## F-001 Capas

Capas independientes dentro de una misma Tile.

---

## F-002 Animaciones

Secuencias de Tiles animadas.

---

## F-003 Herramientas de Simetría

```text
Horizontal
Vertical
Quadrant
```

---

## F-004 Paletas Inteligentes

Actualización automática de colores compartidos.

---

## F-005 Generación Automática de Variantes

Creación de versiones alternativas de una Tile.

---

## F-006 Exportación Avanzada

Exportación optimizada de:

```text
PNG

Sprite Sheets

Texture Atlas

Metadata Files
```
