% Ejercicio 8

include "globals.mzn";

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

array[1..(numTareas+1)] of string: nombreTareas = ["0", "A", "B", "C", "D", "E", "F", "G", "H", "I"];

array[1..numTareas,1..2] of int: predecesoras = [|Ninguna,Ninguna,| A,Ninguna,| B,Ninguna,| A,Ninguna| C,D| C,D| C,D| A,Ninguna| F,H|];

array[1..(numTareas+1)] of int: duracion = [1,7,3,1,8,2,1,1,3,2];

int: tiempoMaximo = sum(i in 1..(numTareas+1))(duracion[i]);

array[1..(numTareas+1)] of int: trabajadoresNece = [0,2,3,2,2,1,2,1,1,2];


array[1..(numTareas+1)] of var 0..tiempoMaximo: inicios;

array[1..numTrabajadores,1..tiempoMaximo] of var 1..(numTareas+1): asigTrabajadores;

array[1..numTrabajadores,1..numTareas] of var bool: tareasTrabajadores;

array[1..numTrabajadores] of var bool: trabajadoresLibres;

var int: asignados;

var int: tiempoMinimo;

% Definición de restricciones:

predicate obtenerTrabajadoresAsig(int: tarea, var int: asignados) =
  asignados = 0 /\
  forall(i in 1..numTrabajadores)(if tareasTrabajadores[i,tarea] then asignados = asignados + 1 else asignados = asignados endif)
;

constraint inicios[1] == 0;

constraint forall(i in A..(numTareas+1))(inicios[i] >= max(j in 1..2)(duracion[predecesoras[i-1,j]] + inicios[predecesoras[i-1,j]]));

  % Comprueba que hay los trabajadores necesarios
constraint forall(i in 1..numTareas)(if trabajadoresNece[i+1] == 3 then forall(j in 1..numTrabajadores)(tareasTrabajadores[j,i] == true) else if trabajadoresNece[i+1] == 2 then    (tareasTrabajadores[1,i] == true /\ tareasTrabajadores[2,i] == true /\ tareasTrabajadores[3,i] == false) \/ (tareasTrabajadores[1,i] == true /\ tareasTrabajadores[2,i] == false /\ tareasTrabajadores[3,i] == true) \/ (tareasTrabajadores[1,i] == false /\ tareasTrabajadores[2,i] == true /\ tareasTrabajadores[3,i] == true) else (tareasTrabajadores[1,i] == true /\ tareasTrabajadores[2,i] == false /\ tareasTrabajadores[3,i] == false) \/ (tareasTrabajadores[1,i] == false /\ tareasTrabajadores[2,i] == true /\ tareasTrabajadores[3,i] == false) \/ (tareasTrabajadores[1,i] == false /\ tareasTrabajadores[2,i] == false /\ tareasTrabajadores[3,i] == true) endif endif);

  % Comprueba si concuerdan las asignaciones con las tareas que realiza cada trabajador
constraint forall(i in 1..numTrabajadores, j in 1..numTareas)(if tareasTrabajadores[i,j] == true then forall(z in inicios[j+1]..(inicios[j+1]+duracion[j+1]-1))(asigTrabajadores[i,z] == (j+1)) else forall(z in inicios[j+1]..(inicios[j+1]+duracion[j+1]-1))(asigTrabajadores[i,z] == Ninguna) endif);

constraint tiempoMinimo == inicios[I] + duracion[I];

% Indica que es un CSP
solve minimize tiempoMinimo;

% Salida por pantalla

output ["TIEMPO MINIMO = \(tiempoMinimo)\n"] ++
       ["Tarea \(nombreTareas[i]) empieza en tiempo \(inicios[i])\n" | i in 2..(numTareas+1)];
