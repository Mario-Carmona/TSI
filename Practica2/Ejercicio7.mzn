% Ejercicio 7

include "globals.mzn";

% Definición de variables:

  % Número de tareas
int: numTareas = 12;

  % Variables que indican que número identifica a cada tareas
  % Se ha incluido la tarea Ninguna, que representa el inicio del proceso, además sirve para asignarla como predecesora para algunas asignaturas
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
int: J = 11;
int: K = 12;
int: L = 13;
int: Final = 14;

  % Vector que indica el inicio de cada tarea
array[1..(numTareas+2)] of var int: inicios;

  % Nombre de las distintas tareas
array[1..(numTareas+1)] of string: nombreTareas = ["Ninguna", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"];

  % Tareas predecesoras de cada una de las tareas del proceso
array[1..numTareas,1..2] of int: predecesoras = [|Ninguna,Ninguna,
                                                 |A,Ninguna,
                                                 |B,Ninguna,
                                                 |C,Ninguna
                                                 |D,Ninguna
                                                 |D,Ninguna
                                                 |D,E
                                                 |C,Ninguna
                                                 |E,F
                                                 |E,F
                                                 |I,J
                                                 |K,Ninguna|];

  % Vector con la duración de cada tareas
array[1..(numTareas+1)] of int: duracion = [1,60,10,5,5,5,2,7,3,7,2,5,1];

  % Variable que contiene el tiempo mínimo para realizar por completo el proceso
var int: tiempoMinimo;


% Definición de restricciones:

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


% Indica que es un COP
solve minimize tiempoMinimo;


% Salida por pantalla

output ["TIEMPO MINIMO = \(tiempoMinimo)\n"] ++
       ["Tarea \(nombreTareas[i]) empieza en tiempo \(inicios[i])\n" | i in 2..(numTareas+1)];
