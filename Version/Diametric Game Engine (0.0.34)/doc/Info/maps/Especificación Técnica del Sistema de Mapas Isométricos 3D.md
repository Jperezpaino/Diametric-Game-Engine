# Especificación Técnica del Sistema de Mapas Isométricos 3D

## 1. Objetivo

Definir una arquitectura de datos para representar mapas isométricos tridimensionales mediante recursos reutilizables y desacoplados.

La arquitectura debe permitir:

* Separar apariencia gráfica y comportamiento lógico.
* Reutilizar recursos visuales y estructurales.
* Facilitar la creación y edición de mapas.
* Reducir la duplicación de información.
* Permitir futuras ampliaciones sin modificar el modelo principal.

Esta especificación define únicamente el modelo conceptual y las relaciones entre sus elementos.

No se establece ningún lenguaje de programación, formato de serialización ni tecnología concreta de implementación.

---

## 2. Principios de Diseño

### Separación de Responsabilidades

```text
Skin      -> Apariencia visual
Material  -> Comportamiento físico
Shape     -> Geometría
Tile      -> Unidad básica del mapa
Structure -> Agrupación reutilizable de Tiles
Map       -> Organización espacial del escenario
```

### Reutilización

Todo recurso debe poder ser reutilizado múltiples veces.

### Extensibilidad

Nuevas funcionalidades deben poder añadirse sin modificar la estructura principal del sistema.

### Independencia Tecnológica

El modelo debe ser independiente de:

* Lenguaje de programación.
* Motor gráfico.
* Sistema operativo.
* Formato de almacenamiento.
* Herramientas de edición.

---

## 3. Sistema de Coordenadas

Todos los elementos espaciales utilizan coordenadas tridimensionales enteras.

```text
X = ancho
Y = largo
Z = altura
```

Representación conceptual:

```text
      Z
      ↑
      │
      │
      └────→ X
     /
    /
   Y
```

---

## 4. Unidad Espacial Básica

La Tile representa la unidad mínima del mapa.

```text
Tile = Unidad espacial mínima
```

No existe ningún elemento de menor tamaño dentro del escenario.

Los elementos complejos se construyen mediante la combinación de múltiples Tiles a través de Structures.

---

## 5. Sistema de Versiones

Todos los recursos definidos por esta especificación deberán incluir un identificador de versión del formato.

Objetivos:

* Facilitar futuras migraciones.
* Mantener compatibilidad con versiones anteriores.
* Permitir la evolución del sistema sin romper recursos existentes.

Ejemplo conceptual:

```text
formatVersion = 1
```

Cada fichero es responsable de indicar la versión del formato que utiliza.

---

## 6. Skin

### Definición

Representa exclusivamente la apariencia gráfica de una Tile.

No contiene comportamiento físico ni información geométrica.

### Propiedades

| Campo | Descripción                   |
| ----- | ----------------------------- |
| id    | Identificador único           |
| name  | Nombre descriptivo            |
| image | Referencia al recurso gráfico |

### Restricciones

* Cada Skin posee un identificador único.
* Un Skin puede ser utilizado por múltiples Tiles.
* Un Skin no define colisiones ni comportamiento.

---

## 7. Material

### Definición

Representa el comportamiento físico de una superficie.

### Ejemplos

* Hierba
* Piedra
* Agua
* Lava
* Hielo
* Barro

### Propiedades

| Campo         | Descripción                          |
| ------------- | ------------------------------------ |
| id            | Identificador único                  |
| name          | Nombre descriptivo                   |
| solid         | Indica si puede atravesarse          |
| damage        | Daño aplicado                        |
| speedModifier | Modificador de velocidad             |
| properties    | Propiedades específicas del material |

### Restricciones

* Un Material puede ser compartido por múltiples Tiles.
* Los materiales deben ser independientes de la apariencia gráfica.

---

## 8. Shape

### Definición

Representa la geometría física de una Tile.

La geometría determina:

* Colisiones.
* Navegación.
* Ascensos.
* Descensos.
* Cálculo de alturas.

### Tipos mínimos

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

### Extensibilidad

El sistema debe permitir la incorporación de nuevas formas geométricas.

---

## 9. Tile

### Definición

Unidad mínima representable dentro del mapa.

### Composición

```text
Tile
 ├── Skin
 ├── Material
 └── Shape
```

### Propiedades

| Campo      | Descripción           |
| ---------- | --------------------- |
| id         | Identificador único   |
| skinId     | Referencia a Skin     |
| materialId | Referencia a Material |
| shape      | Geometría física      |

### Restricciones

* Debe referenciar exactamente un Skin.
* Debe referenciar exactamente un Material.
* Debe poseer exactamente una Shape.

---

## 10. Structure

### Definición

Agrupación reutilizable de Tiles organizada espacialmente.

Su objetivo es evitar duplicación de información y facilitar la creación de elementos complejos.

### Ejemplos

* Columnas
* Casas
* Escaleras
* Árboles
* Muros
* Puentes

### Componentes

Cada elemento interno contiene:

```text
tileId
x
y
z
```

Las coordenadas son relativas al origen de la Structure.

### Origen de una Structure

Toda Structure posee un origen fijo.

```text
Origin = Tile situada en la posición relativa (0,0,0)
```

El punto de anclaje corresponde a la esquina inferior de dicha Tile.

Todas las coordenadas internas se calculan respecto a este origen.

### Restricciones

* Una Structure puede contener cualquier número de Tiles.
* Una Tile puede pertenecer a múltiples Structures.
* Las coordenadas internas siempre son relativas al origen.

---

## 11. Map

### Definición

Representa el escenario final del juego.

Contiene Tiles individuales y estructuras instanciadas.

### Tile Instance

Representa una Tile colocada directamente.

```text
tileId
x
y
z
```

### Structure Instance

Representa una instancia de una Structure.

```text
structureId
x
y
z
```

### Restricciones

* Las coordenadas son absolutas respecto al mapa.
* Una Structure puede instanciarse múltiples veces.
* Las instancias no modifican la definición original.

---

## 12. Sistema de Alturas

El sistema debe soportar múltiples niveles de altura utilizando el eje Z.

Ejemplo conceptual:

```text
Z=3  █ █ █
Z=2  █ █ █
Z=1  █ █ █
Z=0  █ █ █
```

Esto permite representar:

* Edificios de varias plantas.
* Acantilados.
* Cuevas.
* Puentes.
* Plataformas elevadas.

---

## 13. Escalabilidad

La arquitectura debe permitir mapas de gran tamaño.

Por tanto:

* Los recursos se definen una única vez.
* Los recursos se reutilizan mediante referencias.
* Debe minimizarse la duplicación de datos.
* Debe ser posible implementar particionado espacial (chunks) sin modificar el modelo conceptual.

---

# Ampliaciones Futuras

Las siguientes características no forman parte del núcleo inicial, pero el diseño actual debe permitir incorporarlas en versiones posteriores.

---

## A. Sistema de Variantes

Objetivo:

Reducir la repetición visual utilizando múltiples representaciones gráficas para una misma Tile.

Ejemplo:

```text
Grass

 ├── grass_01
 ├── grass_02
 ├── grass_03
 └── grass_04
```

Todas las variantes comparten:

* Material.
* Shape.
* Comportamiento.

Únicamente cambia la representación gráfica.

---

## B. Sistema de Auto-Tiling

Objetivo:

Seleccionar automáticamente la apariencia visual adecuada según las Tiles vecinas.

Ejemplo:

```text
Water
```

Puede generar automáticamente:

```text
Centro
Borde Norte
Borde Sur
Borde Este
Borde Oeste
Esquina Interior
Esquina Exterior
```

El diseñador únicamente coloca Tiles y el sistema determina la representación gráfica apropiada.

---

## C. Sistema de Layers

Objetivo:

Separar elementos lógicos y visuales que ocupan la misma posición espacial.

Ejemplo conceptual:

```text
GROUND
DECORATION
OBJECT
EFFECT
```

Esto permitirá:

* Facilitar el trabajo del editor.
* Organizar el renderizado.
* Mostrar u ocultar categorías de elementos.
* Gestionar decoraciones y efectos sin modificar el terreno base.

Los Layers son independientes del sistema de altura (Z).

Dos elementos pueden compartir la misma posición espacial y pertenecer a Layers distintos.

```
```
