% Ejercicio 10

include "globals.mzn";

% Definición de variables:

  % Peso máximo
int: pesoMaximo = 5000;

  % Variable que contiene la suma del pesos de todos los objetos escogidos
var int: sumaPeso;

  % Variable que contiene la suma de las preferencias de todos los objetos escogidos
var int: sumaPreferencias;

  % Número de objetos
set of int: numObjetos = 1..12;

  % Nombre de los distintos objetos a escoger
array[numObjetos] of string: nombres = ["Mapa", "Cuchillo", "Prismáticos", "Agua", "Linterna", "Cerveza", "Novela", "Manual de Supervivencia", "Plátanos", "Caramelos", "Cerillas", "Espejo"];

  % Vector con los pesos de cada objeto
array[numObjetos] of int: pesos = [50, 500, 1000, 3000, 1500, 3000, 500, 1000, 2000, 500, 250, 500];

  % Vector con las preferencias de cada objeto
array[numObjetos] of int: preferencias = [100, 90, 75, 95, 85, 10, 5, 70, 90, 20, 50, 40];
  
  % Vector en el que se marca con un 1 si ha sido elegido el objeto de la posición donde está el 1, o 0 si no ha sido elegido
array[numObjetos] of var 0..1: elegidos;


% Definición de restricciones:

  % Se calcula cuál es la suma de preferencias
constraint sumaPreferencias == sum(i in numObjetos)(elegidos[i] * preferencias[i]);

  % Se calcular cuál es la suma de los pesos
constraint sumaPeso == sum(i in numObjetos)(elegidos[i] * pesos[i]);

  % Se comprueba que la suma de pesos no supera el máximo
constraint sumaPeso <= pesoMaximo;


% Indica que es un COP
solve maximize sumaPreferencias;


% Salida por pantalla

output ["Objetos = "] ++
       [if fix(elegidos[i])==1 then "\(nombres[i]) " else "" endif | i in numObjetos] ++
       [" |  Preferencias = ", show(sumaPreferencias)] ++
       ["  |  PesoFinal = ", show(sumaPeso), " kgs"];
