# Prácticas TSI
*Contiene todas la prácticas realizadas en la asignatura TSI en la UGR durante el curso 20/21*

## [Práctica 1: Técnicas de Búsqueda Heurística](./Practica1)

Para esta práctica se ha hecho uso del entorno GVGAI para la creación de un agente que resuelve niveles del juego Boulder Dash, pero modificando ciertas reglas del juego original.

Las diferencias respecto al juego original son las siguientes:

- Todos los enemigos están liberados desde el comienzo.
- No hay rocas que puedan caer sobre el avatar.

El agente debe tener los siguientes comportamientos:

- **Comportamiento deliberativo simple**: búsqueda del camino óptimo al portal (sin enemigos, pero con la posible presencia de obstáculos).
- **Comportamiento deliberativo compuesto**: búsqueda de 9 gemas (en un mapa con un número superior o igual de gemas) y salida por el portal.
- **Comportamiento reactivo simple**: mantenerse alejado de un enemigo durante un tiempo predeterminado (2000 ticks).
- **Comportamiento reactivo compuesto**: mantenerse alejado de dos enemigos durante un tiempo predeterminado (2000 ticks).
- **Comportamiento reactivo-deliberativo**: búsqueda de 9 gemas (en un mapa con un número superior o igual de gemas), evitando el enemigo presente en el mapa y, una vez se tengan todas, alcanzar el portal dentro de los límites de tiempo predeterminados (2000 ticks).

## 
