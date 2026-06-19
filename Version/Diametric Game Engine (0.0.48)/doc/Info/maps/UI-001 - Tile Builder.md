# UI-001 - Tile Builder

## 1. Objetivo

Tile Builder es la herramienta responsable de la gestión de recursos básicos del sistema de mapas.

Su finalidad es permitir la creación, modificación y almacenamiento de definiciones de Tile mediante la combinación de:

* Skin (apariencia visual)
* Material (comportamiento físico)
* Shape (geometría)

La aplicación genera como resultado los ficheros de definición de Tiles utilizados posteriormente por Structure Builder y Map Builder.

---

# 2. Ventana Principal

La aplicación estará compuesta por una única ventana principal de trabajo.

Características:

| Propiedad         | Valor        |
| ----------------- | ------------ |
| Título            | Tile Builder |
| Tamaño mínimo     | 640 x 480    |
| Redimensionable   | Sí           |
| Maximizable       | Sí           |
| Minimizable       | Sí           |
| Pantalla completa | Opcional     |
| Barra de estado   | Sí           |

Cuando no exista ningún fichero abierto se mostrará una pantalla de bienvenida con una imagen conceptual representativa de la aplicación.

Esta imagen tendrá únicamente carácter informativo y no afectará a la funcionalidad del programa.

---

# 3. Distribución General

La ventana principal estará organizada mediante cinco áreas principales.

```text
┌──────────────────────────────────────────────────────────────┐
│ Menu Bar                                                     │
├──────────────────────────────────────────────────────────────┤
│ Toolbar                                                      │
├───────────────┬───────────────────────┬──────────────────────┤
│               │                       │                      │
│ Tile Library  │      Tile Preview     │     Properties       │
│               │                       │                      │
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

Proporcionar acceso a todas las funcionalidades globales de la aplicación.

---

### Archivo (File)

#### Nuevo fichero de Tiles

```text
File
 └─ New Tile File
```

Atajo:

```text
Ctrl + N
```

Acción:

* Crea un nuevo proyecto de Tiles.
* Elimina el contenido actual no guardado previa confirmación del usuario.

---

#### Abrir fichero de Tiles

```text
File
 └─ Open Tile File
```

Atajo:

```text
Ctrl + O
```

Acción:

* Permite seleccionar un fichero de Tiles existente.
* Carga todas las definiciones asociadas.

---

#### Guardar fichero de Tiles

```text
File
 └─ Save Tile File
```

Atajo:

```text
Ctrl + S
```

Acción:

* Guarda el fichero actual.

---

#### Guardar como

```text
File
 └─ Save As Tile File
```

Acción:

* Guarda el proyecto con un nombre diferente.

---

#### Cerrar

```text
File
 └─ Close
```

Atajo:

```text
Ctrl + W
```

Acción:

* Cierra el proyecto actual.

---

#### Salir

```text
File
 └─ Exit
```

Atajo:

```text
Alt + F4
```

Acción:

* Cierra la aplicación.

---

### Tools

Responsabilidad:

Gestionar recursos globales.

Opciones:

```text
Tools
 ├─ Skin Manager
 ├─ Material Manager
 └─ Refresh Resources
```

---

### Help

Opciones:

```text
Help
 └─ About Tile Builder
```

Atajo:

```text
F1
```

---

# 4.2 Toolbar

Responsabilidad:

Acceso rápido a operaciones frecuentes.

Botones mínimos:

```text
New
Open
Save

Add Tile
Duplicate Tile
Delete Tile
```

Cada botón deberá mostrar:

* Icono
* Texto descriptivo
* Tooltip

---

# 4.3 Tile Library

Ubicación:

Panel izquierdo.

Responsabilidad:

Mostrar todas las Tiles definidas dentro del proyecto.

Contenido:

```text
Tile 001
Tile 002
Tile 003
...
```

Funciones:

* Seleccionar Tile.
* Crear Tile.
* Duplicar Tile.
* Eliminar Tile.
* Buscar Tile.
* Ordenar Tiles.

---

# 4.4 Tile Preview

Ubicación:

Zona central.

Responsabilidad:

Visualizar la Tile seleccionada.

Contenido mínimo:

* Imagen asociada.
* Nombre.
* Material.
* Shape.

Representación inicial:

```text
┌─────────────┐
│             │
│  Preview    │
│             │
└─────────────┘
```

Posibles ampliaciones futuras:

* Vista isométrica.
* Vista tridimensional.
* Vista de variantes.

---

# 4.5 Properties Panel

Ubicación:

Panel derecho.

Responsabilidad:

Editar las propiedades de la Tile seleccionada.

Campos:

---

## Nombre

```text
Name
```

Tipo:

Texto.

---

## Skin

```text
Skin
```

Tipo:

Lista desplegable.

Origen:

Skin Manager.

---

## Material

```text
Material
```

Tipo:

Lista desplegable.

Origen:

Material Manager.

---

## Shape

```text
Shape
```

Tipo:

Lista desplegable.

Valores mínimos:

```text
FLAT
WALL

SLOPE_NORTH
SLOPE_SOUTH
SLOPE_EAST
SLOPE_WEST

STAIR_UP
STAIR_DOWN
```

---

# 4.6 Status Bar

Ubicación:

Parte inferior.

Responsabilidad:

Mostrar información contextual.

Información mínima:

```text
Proyecto actual
Número de Tiles
Tile seleccionada
Estado de guardado
```

Ejemplo:

```text
tiles.json | 24 Tiles | Tile_Grass | Saved
```

---

# 5. Ventanas Secundarias

La aplicación utilizará ventanas auxiliares para la gestión de recursos globales.

---

# 5.1 Skin Manager

Responsabilidad:

Administrar recursos gráficos.

Distribución:

```text
┌─────────────────────────────┐
│ Skin Manager                │
├─────────────────────────────┤
│ Skin List                   │
│                             │
│ grass.png                   │
│ stone.png                   │
│ water.png                   │
│                             │
├─────────────────────────────┤
│ Preview                     │
├─────────────────────────────┤
│ Import                      │
│ Delete                      │
└─────────────────────────────┘
```

Funciones:

* Importar imágenes.
* Eliminar imágenes.
* Renombrar recursos.
* Visualizar recursos.

---

# 5.2 Material Manager

Responsabilidad:

Administrar materiales.

Distribución:

```text
┌─────────────────────────────┐
│ Material Manager            │
├─────────────────────────────┤
│ Material List               │
├─────────────────────────────┤
│ Properties                  │
├─────────────────────────────┤
│ New                         │
│ Edit                        │
│ Delete                      │
└─────────────────────────────┘
```

Propiedades mínimas:

```text
Name
Solid
Damage
Speed Modifier
```

Funciones:

* Crear material.
* Modificar material.
* Eliminar material.

---

# 6. Flujo de Trabajo

El flujo principal esperado será:

```text
Nuevo Proyecto
        ↓
Importar Skins
        ↓
Crear Materiales
        ↓
Crear Tiles
        ↓
Validar Recursos
        ↓
Guardar Fichero de Tiles
```

---

# 7. Restricciones

* Toda Tile debe tener exactamente un Skin.
* Toda Tile debe tener exactamente un Material.
* Toda Tile debe tener exactamente una Shape.
* No se permitirá guardar Tiles incompletas.
* No se permitirá eliminar recursos utilizados sin confirmación previa.
* Los cambios no guardados deberán ser detectados automáticamente.

---

# 8. Compatibilidad Futura

La interfaz deberá diseñarse permitiendo futuras ampliaciones:

* Sistema de Variantes.
* Auto-Tiling.
* Layers.
* Vista Isométrica Avanzada.
* Integración con Structure Builder.

Estas ampliaciones no deberán requerir modificaciones estructurales significativas en la ventana principal.
"""
