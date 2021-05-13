% Ejercicio 7

include "globals.mzn";

% Definición de variables:

int: numTareas = 12;

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

array[1..(numTareas+1)] of var int: inicios;

array[1..(numTareas+1)] of string: nombreTareas = ["Ninguna", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"];

array[1..numTareas,1..2] of int: predecesoras = [|Ninguna,Ninguna,| A,Ninguna,| B,Ninguna,| C,Ninguna| D,Ninguna| D,Ninguna| D,E| C,Ninguna| E,F| E,F| I,J| K,Ninguna|];

array[1..(numTareas+1)] of int: duracion = [0,60,10,5,5,5,2,7,3,7,2,5,1];

var int: tiempoMinimo;

% Definición de restricciones:

constraint inicios[1] == 0;
constraint inicios[2] == 1;

constraint forall(i in 3..(numTareas+1))(inicios[i] == max(j in 1..2)(duracion[predecesoras[i-1,j]] + inicios[predecesoras[i-1,j]]));

constraint tiempoMinimo == inicios[L] + duracion[L];

% Indica que es un CSP
solve satisfy;

% Salida por pantalla

output ["TIEMPO MINIMO = \(tiempoMinimo)\n"] ++
       ["Tarea \(nombreTareas[i]) empieza en tiempo \(inicios[i])\n" | i in 2..(numTareas+1)];
