% Ejercicio 1

include "globals.mzn";

% Definición de variables:

  % Número de letras
set of int: POS = 1..9;

  % Variables que representan a cada letra
var 0..8: S;
var 0..8: I;
var 0..8: X;
var 0..8: E;
var 0..8: V;
var 0..8: N;
var 0..8: T;
var 0..8: W;
var 0..8: Y;

  % Variables que representan a los número usados en la operación
var int: SIX = S*100+I*10+X;
var int: SEVEN = S*10000+E*1000+V*100+E*10+N;
var int: TWENTY = T*100000+W*10000+E*1000+N*100+T*10+Y;

  % Matriz con el valor de cada letra
array[POS] of var 0..8: letras = [S,I,X,E,V,N,T,W,Y];


% Definición de restricciones:

  % Comprobar que cada letra tiene un valor distinto
constraint all_different(letras);

  % Comprobar que el calculo de la operación es correcto
constraint TWENTY == (SIX + 2*SEVEN);


% Indica que es un CSP
solve satisfy;


% Salida por pantalla
output ["\t   ", show(SIX), 
        "\n\t+", show(SEVEN), 
        "\n\t+", show(SEVEN),
        "\n\t------",
        "\n\t", show(TWENTY)];
