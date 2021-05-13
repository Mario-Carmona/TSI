% Ejercicio 10

include "globals.mzn";

% Definición de variables:

int: pesoMaximo = 5000;
var int: sumaPeso;
var int: sumaPreferencias;
set of int: numObjetos = 1..12;
array[numObjetos] of string: nombres = ["Mapa", "Cuchillo", "Prismáticos", "Agua", "Linterna", "Cerveza", "Novela", "Manual de Supervivencia", "Plátanos", "Caramelos", "Cerillas", "Espejo"];
array[numObjetos] of int: pesos = [50, 500, 1000, 3000, 1500, 3000, 500, 1000, 2000, 500, 250, 500];
array[numObjetos] of int: preferencias = [100, 90, 75, 95, 85, 10, 5, 70, 90, 20, 50, 40];
array[numObjetos] of var 0..1: elegidos;

% Definición de restricciones:

constraint sumaPreferencias == sum(i in numObjetos)(elegidos[i] * preferencias[i]);
constraint sumaPeso == sum(i in numObjetos)(elegidos[i] * pesos[i]);
constraint pesoMaximo >= sumaPeso;


% Indica que es un COP
solve maximize sumaPreferencias;

% Salida por pantalla

output ["Objetos = "] ++
       [if fix(elegidos[i])==1 then "\(nombres[i]) " else "" endif | i in numObjetos] ++
       [" |  Preferencias = ", show(sumaPreferencias)] ++
       ["  |  PesoFinal = ", show(sumaPeso), " kgs"];
