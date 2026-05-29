Quiero que actúes como arquitecto de software y diseñador técnico con un enfoque 100% orientado a implementación real mediante Copilot.

Necesito definir la arquitectura y el plan de desarrollo de un motor gráfico/juego en Java utilizando exclusivamente Graphics2D como tecnología base de renderizado, con proyección diamétrica 2:1.

La respuesta debe estar diseñada para que después pueda usarla como base de trabajo con el propio Copilot y pedirle que implemente cada fase, módulo o conjunto de clases de manera incremental. No quiero una propuesta solo conceptual: quiero una guía técnica realista, modular, construible y utilizable como hoja de ruta de implementación.

## Objetivo del proyecto
Construir desde cero un motor funcional en Java con Graphics2D capaz de renderizar mapas en proyección diamétrica 2:1, gestionar entidades, controlar cámara, entrada de usuario, colisiones, interacciones, enemigos y navegación básica, dejando la base preparada para evolucionar más adelante hacia sistemas visuales y de jugabilidad más avanzados.

## Contexto y restricciones técnicas
- El proyecto se desarrollará en Java.
- El renderizado debe realizarse exclusivamente con Graphics2D.
- No quiero depender de motores externos ni frameworks de juego.
- Se puede usar la librería estándar de Java y, si lo consideras razonable, utilidades mínimas y comunes, pero la propuesta debe centrarse en una base propia.
- La prioridad inicial es construir un núcleo sólido, claro, modular y mantenible.
- En la primera fase, tiles, personajes y elementos visuales pueden representarse mediante dibujo simple con Graphics2D, sin necesidad de sprites finales.
- Más adelante el motor deberá poder crecer hacia:
  - sprites e imágenes reales,
  - animaciones,
  - iluminación,
  - audio,
  - partículas,
  - efectos visuales,
  - IA más avanzada.
- El proyecto debe estar extensamente documentado desde el principio.
- La documentación debe existir tanto en el propio código como en documentos adicionales en formato Markdown (`*.md`).
- La documentación debe evolucionar junto con la implementación y formar parte de cada fase del desarrollo.

## Requisitos funcionales mínimos del motor
El motor debe contemplar como mínimo las siguientes capacidades:

1. Soporte para múltiples resoluciones definibles mediante configuración.
2. Configuración independiente de FPS (render) y UPS (actualización lógica).
3. Gestión del ciclo de vida del juego:
   - inicialización,
   - carga,
   - actualización,
   - renderizado,
   - pausa,
   - cierre.
4. Sistema de entrada por teclado y ratón.
5. Movimiento del personaje e interacción con el entorno y con el mapa.
6. Sistema de mapas en proyección diamétrica 2:1 basado en tiles/celdas.
7. Representación lógica del mapa en tres dimensiones:
   - ancho,
   - fondo,
   - altura.
8. Soporte para mapas de tamaño variable, incluidos mapas más grandes que el área visible.
9. Cámara que intente mantener al personaje centrado, salvo cuando se alcancen los límites del mapa.
10. Capas no visibles del mapa para controlar:
   - colisiones,
   - bloqueos,
   - paredes,
   - rampas o desniveles,
   - cambios de plano o altura.
11. Interacción del personaje con elementos configurables del mapa.
12. Enemigos con comportamiento básico:
   - detección por proximidad,
   - radio de visión o detección configurable,
   - desplazamiento por el mapa.
13. Pathfinding para enemigos mediante A*.
14. Sistema de eventos o acciones configurables en el mapa, por ejemplo:
   - cambio de mapa al atravesar puertas,
   - activación de interruptores,
   - desencadenadores de eventos.
15. Base técnica preparada para ampliaciones futuras sin necesidad de rehacer la arquitectura principal.

## Lo que necesito que me entregues
Quiero una propuesta técnica orientada a construcción real. La respuesta debe servirme como base directa para seguir implementando el proyecto con Copilot.

### 1. Resumen ejecutivo
Explica brevemente cuál sería la estrategia general recomendada para construir el motor de forma incremental, sin sobrediseño y con una base estable para crecimiento futuro.

### 2. Arquitectura general implementable
Define una arquitectura realista, construible desde cero y pensada para implementarse en iteraciones pequeñas.
No quiero una arquitectura excesivamente abstracta, académica o sobredimensionada.
Quiero una estructura que permita empezar a programar de inmediato y crecer sin romper lo ya construido.

### 3. Módulos o subsistemas principales
Define los módulos principales del sistema.
Para cada módulo indica:
- responsabilidad,
- qué problema resuelve,
- clases e interfaces sugeridas,
- dependencias principales,
- orden recomendado de implementación.

Como mínimo quiero que consideres, si aplican:
- núcleo del motor,
- game loop,
- render,
- proyección diamétrica,
- mapas,
- tiles/celdas,
- cámara,
- input,
- entidades,
- jugador,
- enemigos,
- colisiones,
- navegación/pathfinding,
- eventos/interacciones,
- configuración,
- carga/gestión de recursos.

### 4. Estructura inicial del proyecto
Propón una estructura de paquetes o carpetas para un proyecto Java real.
Debe estar pensada para:
- implementación incremental,
- buena organización,
- separación clara de responsabilidades,
- facilidad para que Copilot genere código coherente por módulos.

### 5. Clases e interfaces base mínimas
Propón el conjunto mínimo de clases e interfaces necesarias para arrancar correctamente.
No quiero una lista enorme, sino una base pequeña pero bien pensada.

Para cada clase o interfaz indica:
- nombre sugerido,
- propósito,
- responsabilidad principal,
- relación con otras piezas,
- prioridad de implementación:
  - inmediata,
  - siguiente fase,
  - posterior.

### 6. Decisiones técnicas clave desde el inicio
Quiero que definas claramente las decisiones que deben fijarse pronto para evitar rehacer trabajo más adelante, especialmente en:
- representación de coordenadas del mundo,
- conversión de coordenadas del mapa a pantalla,
- conversión inversa si aplica,
- orden de pintado en proyección diamétrica,
- separación entre lógica y render,
- estructura interna del mapa,
- modelado de tiles y capas,
- manejo de altura,
- sistema de colisiones,
- cámara,
- eventos,
- pathfinding.

Si existen varias alternativas, elige una recomendación principal y explica brevemente por qué.

### 7. Roadmap por fases
Divide el desarrollo en fases pequeñas, acumulativas y construibles.
Cada fase debe incluir:
- objetivo,
- funcionalidades a implementar,
- módulos involucrados,
- clases principales,
- resultado funcional visible,
- criterio de finalización,
- documentación mínima que debe quedar completada en esa fase.

Importante: cada fase debe poder implementarse de forma independiente y dejar el proyecto en un estado compilable y ejecutable, aunque sea con funcionalidad parcial.

### 8. Definición del MVP real
Define con claridad qué debe incluir el producto mínimo viable del motor.
Quiero una separación explícita entre:
- imprescindible para el MVP,
- importante pero no esencial,
- ampliaciones posteriores.

Ese MVP debe permitir como mínimo:
- abrir una ventana,
- ejecutar el loop principal,
- renderizar un mapa diamétrico básico,
- mover un personaje,
- aplicar cámara,
- detectar colisiones simples.

### 9. Riesgos y retos técnicos
Identifica las partes más delicadas del proyecto y explica por qué pueden generar problemas si no se diseñan bien desde el principio.
Quiero especial atención a:
- proyección diamétrica,
- orden de pintado,
- cálculo de profundidad visual,
- colisiones con altura o desniveles,
- sincronización entre update y render,
- pathfinding sobre mapas con restricciones,
- escalabilidad del sistema de entidades,
- extensibilidad futura.

### 10. Recomendaciones de implementación práctica
Indica buenas prácticas específicas para este proyecto en Java con Graphics2D, especialmente sobre:
- organización del código,
- modularidad,
- rendimiento,
- depuración,
- configuración,
- pruebas,
- evolución progresiva del diseño.

### 11. Estrategia de documentación
Define cómo debe documentarse el proyecto desde la primera iteración.

Incluye obligatoriamente:
- documentación en el propio código,
- documentación técnica en archivos `.md`,
- documentación funcional en archivos `.md`,
- documentación de arquitectura,
- documentación del flujo de ejecución,
- documentación de módulos y responsabilidades,
- documentación del roadmap y del estado del proyecto.

Quiero que indiques también:
- qué documentos `.md` deberían existir desde el inicio,
- qué contenido debería tener cada uno,
- qué documentación debe actualizarse en cada fase,
- cómo mantener la documentación sincronizada con la implementación,
- y qué nivel de detalle deben tener los comentarios en código para que el proyecto sea entendible y mantenible por terceros.

### 12. Primera iteración lista para construir con Copilot
Define una primera iteración extremadamente concreta, pequeña y ejecutable, pensada para implementarse directamente con ayuda de Copilot.

Debe incluir:
- objetivo exacto,
- lista de clases a crear primero,
- responsabilidad de cada clase,
- orden de implementación recomendado,
- resultado esperado al finalizar,
- documentación en código que debería incluirse,
- y documentos `.md` mínimos que deberían crearse o actualizarse en esa primera iteración.

La primera iteración debe dejar algo visible, ejecutable y documentado, aunque sea simple.

### 13. Preparación para implementación asistida por Copilot
Quiero que estructures la propuesta para que luego pueda pedirle a Copilot cosas como:
- “implementa la fase 1”,
- “crea la estructura base del proyecto”,
- “genera las clases del game loop”,
- “implementa el sistema de cámara”,
- “añade el módulo de input”,
- “crea la base de mapas diamétricos”,
- “genera la documentación inicial del proyecto”,
sin tener que redefinir toda la arquitectura.

Por eso, usa nombres claros, límites bien definidos y módulos realmente implementables.

## Restricciones importantes para tu respuesta
- No quiero una respuesta genérica.
- No quiero solo ideas o conceptos abstractos.
- No quiero una arquitectura académica difícil de bajar a código.
- No quiero sobrediseño.
- Quiero una base realista, mantenible y preparada para ser implementada con Copilot paso a paso.
- Prioriza claridad, modularidad, bajo acoplamiento y capacidad de crecimiento.
- Si propones patrones de diseño, usa solo los que sean realmente útiles y justificados.
- Si detectas decisiones que conviene simplificar en una primera versión, indícalo claramente.
- Supón que el proyecto empieza completamente desde cero.
- Toda fase o implementación propuesta debe incluir también la documentación mínima correspondiente, tanto en código como en archivos Markdown, de forma que el proyecto evolucione siempre acompañado de documentación actualizada.

## Formato de salida obligatorio
Organiza la respuesta exactamente en este orden:

1. Resumen ejecutivo
2. Arquitectura general
3. Módulos del sistema
4. Estructura de paquetes
5. Clases e interfaces base mínimas
6. Decisiones técnicas clave
7. Roadmap por fases
8. Definición del MVP
9. Riesgos técnicos
10. Recomendaciones prácticas
11. Estrategia de documentación
12. Primera iteración lista para implementar
13. Prompt de continuación para implementación

## Requisito final muy importante
En el último apartado, “Prompt de continuación para implementación”, redacta el texto exacto que después podré pasar a Copilot para que implemente la primera fase del proyecto de forma coherente con toda la arquitectura propuesta.

Ese prompt final debe:
- estar listo para copiar y pegar,
- pedir implementación real,
- indicar claramente alcance y límites,
- exigir que el resultado sea compilable y ejecutable,
- e incluir también la generación o actualización de la documentación necesaria en código y en archivos Markdown (`*.md`).