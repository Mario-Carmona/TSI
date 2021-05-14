% Ejercicio 8

include "globals.mzn";
include "cumulative.mzn";

% Definición de variables:

int: numTareas = 9;
int: numTrabajadores = 3;

int: Ninguna = 1;
int: A = 2;
int: B = 3;
int: C = 4;
int: D = 5;
int: E = 6;
int: F = 7;
int: G = 8;
int: H = 9;
int: I = 10;
int: Final = 11;

array[1..(numTareas+2)] of string: nombreTareas = ["0", "A", "B", "C", "D", "E", "F", "G", "H", "I", ""];

array[1..numTareas,1..2] of int: predecesoras = [|Ninguna,Ninguna,
                                                 |A,Ninguna,
                                                 |B,Ninguna,
                                                 |A,Ninguna
                                                 |C,D
                                                 |C,D
                                                 |C,D
                                                 |A,Ninguna
                                                 |F,H|];

array[1..(numTareas+2)] of var int: duracion = [1,7,3,1,8,2,1,1,3,2,0];

int: tiempoMaximo = 29;

array[1..(numTareas+2)] of var int: trabajadoresNece = [0,2,3,2,2,1,2,1,1,2,0];


array[1..(numTareas+2)] of var 0..tiempoMaximo: inicios;

array[1..numTrabajadores,1..tiempoMaximo] of var 1..(numTareas+2): asigTrabajadores;

%array[1..numTrabajadores,1..numTareas] of var bool: tareasTrabajadores;

%array[1..numTrabajadores] of var bool: trabajadoresLibres;

%var int: asignados;

var int: tiempoMinimo;

% Definición de restricciones:

  % Se realiza la asignación del inicio de cada tarea cumpliendo la restricción del número de trabajadores que se necesita, y sin usar más trabajadores de los que se tiene
constraint cumulative(inicios, duracion, trabajadoresNece, numTrabajadores);

  % Se indica que la tarea Ninguna empieza en el tiempo 0, para que cualquier tarea tenga un inicio posterior y no cause problemas como tarea predecesora
constraint inicios[Ninguna] == 0;

  % Se comprueba que el inicio del resto de tareas es siempre mayor o igual a la finalización de todas las tareas predecesoras de la tarea que se está examinando
constraint 
forall(i in A..(numTareas+1))(
  forall(j in 1..2)(
    inicios[i] >= (inicios[predecesoras[i-1,j]] + duracion[predecesoras[i-1,j]])
  )
);

  % Se comprueba cual es el tiempo en que terminan todas las tareas
constraint
forall(i in A..(numTareas+1))(
  inicios[Final] >= (inicios[i] + duracion[i])
);

  % Se indica que el tiempo mínimo es igual al tiempo en que han finalizado todas las tareas
constraint tiempoMinimo == inicios[Final];

  % Se asignan las tareas a los distintos trabajadores
constraint
forall(asig in A..I)(
  (forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))(
    asigTrabajadores[1,j] == asig 
  )
  \/
  forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))( 
    asigTrabajadores[2,j] == asig 
  )
  \/ 
  forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))(
    asigTrabajadores[3,j] == asig
  ))
    /\
  forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))(
    sum(i in 1..numTrabajadores)(if asigTrabajadores[i,j] == asig then 1 else 0 endif) == trabajadoresNece[asig]
  )
);

constraint
forall(i in 1..numTrabajadores)(
  forall(j in tiempoMinimo..tiempoMaximo)(
    asigTrabajadores[i,j] == (numTareas+2)
  )
);


% Indica que es un COP
solve minimize tiempoMinimo;

% Salida por pantalla

output ["TIEMPO MINIMO = \(tiempoMinimo)\n*****\n"] ++
       ["Tarea \(nombreTareas[i]) empieza en tiempo \(inicios[i])\n" | i in 2..(numTareas+1)]++
       ["*****\nAsignacion:\n"]++
       ["\(nombreTareas[fix(asigTrabajadores[1,i])]) " | i in 1..tiempoMaximo]++
       ["\n"]++
       ["\(nombreTareas[fix(asigTrabajadores[2,i])]) " | i in 1..tiempoMaximo]++
       ["\n"]++
       ["\(nombreTareas[fix(asigTrabajadores[3,i])]) " | i in 1..tiempoMaximo];
