% Ejercicio 8

include "globals.mzn";
include "cumulative.mzn";

% Definición de variables:

  % Número de tareas
int: numTareas = 9;

  % Número de trabajadores
int: numTrabajadores = 3;

  % Variables que indican que número identifica a cada tarea
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
int: Final = 11;

  % Nombre de las distintas tareas
array[1..(numTareas+2)] of string: nombreTareas = ["0", "A", "B", "C", "D", "E", "F", "G", "H", "I", ""];

  % Tareas predecesoras de cada una de las tareas del proceso
array[1..numTareas,1..2] of int: predecesoras = [|Ninguna,Ninguna,
                                                 |A,Ninguna,
                                                 |B,Ninguna,
                                                 |A,Ninguna
                                                 |C,D
                                                 |C,D
                                                 |C,D
                                                 |A,Ninguna
                                                 |F,H|];
  
  % Vector con la duración de cada tarea
array[1..(numTareas+2)] of var int: duracion = [1,7,3,1,8,2,1,1,3,2,0];

  % Tiempo máximo para realizar por completo el proceso
int: tiempoMaximo = 29;

  % Vector con los trabajadores necesarios para realizar cada tarea
array[1..(numTareas+2)] of var int: trabajadoresNece = [0,2,3,2,2,1,2,1,1,2,0];

  % Vector que indica el inicio de cada tarea
array[1..(numTareas+2)] of var 0..tiempoMaximo: inicios;

  % Matriz que representa la asignación de tareas a cada trabajador
array[1..numTrabajadores,1..tiempoMaximo] of var 1..(numTareas+2): asigTrabajadores;

  % Variable que contiene el tiempo mínimo para realizar por completo el proceso
var int: tiempoMinimo;


% Definición de restricciones:

  % Se realiza la asignación del inicio de cada tarea cumpliendo la restricción del número de trabajadores que se necesita, y sin usar más trabajadores de los que se tiene
constraint cumulative(inicios, duracion, trabajadoresNece, numTrabajadores);

  % Se indica que la tarea Ninguna empieza en el tiempo 0, para que cualquier tarea tenga un inicio posterior a esta tarea y no cause problemas con las comprobaciones de las tareas predecesoras
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
  /\
  inicios[Final] <= tiempoMaximo
);

  % Se indica que el tiempo mínimo es igual al tiempo en que han finalizado todas las tareas
constraint tiempoMinimo == inicios[Final];

  % Se comprueba si cada una de las tareas está asignada al número de trabajadores necesarios
constraint
forall(asig in A..I)(
  % De esta forma se asegura que se asigna de forma continua una tarea a un trabajador
  (forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))(
    if j > 0 /\ j <= tiempoMaximo then
      asigTrabajadores[1,j] == asig 
    else false endif
  )
  \/
  forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))( 
    if j > 0 /\ j <= tiempoMaximo then
      asigTrabajadores[2,j] == asig 
    else false endif
  )
  \/ 
  forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))(
    if j > 0 /\ j <= tiempoMaximo then
      asigTrabajadores[3,j] == asig
    else false endif
  ))
    /\
  % De esta forma se comprueba si se asigna al número necesario de trabajadores
  forall(j in inicios[asig]..(duracion[asig]+inicios[asig]-1))(
    if j > 0 /\ j <= tiempoMaximo then
      sum(i in 1..numTrabajadores)(if asigTrabajadores[i,j] == asig then 1 else 0 endif) == trabajadoresNece[asig]
    else false endif
  )
);

  % A todos los tiempos posteriores al tiempo mínimo se le asigna un valor que en la salida será representado como sin asignación, "".
constraint
forall(i in 1..numTrabajadores)(
  forall(j in tiempoMinimo..tiempoMaximo)(
    if j > 0 /\ j <= tiempoMaximo then
      asigTrabajadores[i,j] == (numTareas+2)
    else false endif
  )
);


% Indica que es un COP
solve minimize tiempoMinimo;


% Salida por pantalla
output ["TIEMPO MINIMO = \(tiempoMinimo)\n*****\n"] ++
       ["Tarea \(nombreTareas[i]) empieza en tiempo \(inicios[i])\n" | i in 2..(numTareas+1)]++
       ["*****\nAsignacion:\n"]++
       [" 1   2   3   4   5   6   7   8   9   10  11  12  13  14  15  16  17  18  19  20  21  22 23 24 25 26 27 28 29\n"]++
       ["\(nombreTareas[fix(asigTrabajadores[1,i])]) " | i in 1..tiempoMaximo]++
       ["\n"]++
       ["\(nombreTareas[fix(asigTrabajadores[2,i])]) " | i in 1..tiempoMaximo]++
       ["\n"]++
       ["\(nombreTareas[fix(asigTrabajadores[3,i])]) " | i in 1..tiempoMaximo];
