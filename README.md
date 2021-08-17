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

## Práctica 2: Satisfacción de restricciones

Un problema de satisfacción de restricciones (CSP) se suele expresar como una tupla con tres elementos:

- Un conjunto de variables
- Un conjunto de dominios para las variables
- Un conjunto de restricciones sobre las variables

Un problema de optimización de restricciones (COP) se suele expresar como una tupla con cuatro elementos:

- Los mismos tres elementos de los problemas CSP
- Una función de coste sobre las variables a minimizar/maximizar

En esta práctica se ve como codificar problemas usando un lenguaje de Constraint Programming (CP), paradigma general para resolver muchos CSP.

Los resolvemos usando CP solvers como "caja negra".

Una de las claves está en encontrar una buena representación del problema (variables) que nos permitan fácilmente expresar las restricciones.

Además, una buena representación nos permitirá que el CP solver sea capaz de resolver el problema más eficientemente.

En esta práctica usaremos MiniZinc como lenguaje de modelado de restricciones.

Todas las soluciones de los ejecicios son correctas y completas, es decir, todos los ejercicios dan todas las posibles soluciones correctas.

## Práctica 3: Planificación Clásica (PDDL)

Las técnicas de planificación requieren dos elementos:

- Dominio: Un conjunto de acciones (y sus efectos esperados en el mundo)
- Problema: Una definición del estado inicial y objetivo

Partiendo de una respresentación inicial del mundo (estado inicial), un planificador obtiene una lista de acciones, las cuales, al ser aplicadas en orden, generan una representación del mundo deseada (estado objetivo).

En la planificación hace falta resolver el problema del marco para poder resolverlos. Este problema consiste en saber como cambia el mundo por el efecto de una acción.

Para resolver los ejercicios se hace uso del lenguaje PDDL.

PDDL es un lenguaje estándar para la representación de dominios de planificación clásicos. Basado en LISP.

En PDDL todas las propiedades y relaciones entre los objetos o bien se conocen inicialmente o bien pueden conocerse durante el proceso de planificación:

- Acciones deterministas
- Los efectos de las acciones son conocidos a priori
- Cambios en el mundo son producidos por la ejecución de las acciones
- No se consideran eventos exógenos

Para el desarrollo de esta práctica usaremos el planificados MetricFF en su primera versión.
