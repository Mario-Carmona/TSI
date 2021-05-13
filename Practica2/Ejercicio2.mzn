% Ejercicio 2

include "globals.mzn";

% Definición de variables:

  % Número de tipos de monedas
set of int: POS = 1..8;

  % Importe que se quiere tener
int: importe = 799;

  % Número total de monedas usadas
var int: totalMonedas;

  % Valor en centimos de cada uno de los tipos de moneda
array[POS] of int: valorMonedas = [1,2,5,10,20,50,100,200];
  
  % Cantidad usada de cada tipo de moneda
array[POS] of var int: cantidadMonedas;


% Definición de restricciones:

  % Comprobar que como mínimo se utilizan 0 monedas
constraint 
forall(i in POS)(
  cantidadMonedas[i] >= 0
);

  % Comprobar que la suma del valor de todos las monedas usadas es igual al importe que se quiere tener
constraint importe == sum(i in POS)(cantidadMonedas[i] * valorMonedas[i]);

  % Comprobar que el total de monedas coincide con la suma de la cantidad de monedas de cada tipo que han sido usadas
constraint totalMonedas == sum(i in POS)(cantidadMonedas[i]);


% Indica que es un COP
solve minimize totalMonedas;


% Salida por pantalla
output ["monedas: [" ] ++
       ["\(cantidadMonedas[i]), " | i in 1..7] ++
       [show(cantidadMonedas[8]), "]\n"] ++
       ["total monedas: ", show(totalMonedas)];
