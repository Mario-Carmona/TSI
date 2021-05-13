% Ejercicio 3

include "globals.mzn";

% Definición de variables:

  % Número de colores usados
int: numColores = 5;

  % Número de paises a colorear
int: numPaises = 13;

  % Coste total de colorear el mapa
var int: costeTotal;

  % Asignación de los colores a los países
array[1..numPaises] of var 1..numColores: mapaPaises;

  % Coste de cada color
array[1..numColores] of int: precioColores = [100, 200, 300, 400, 500];

  % Nombre de cada color
array[1..numColores] of string: nombreColores = ["Verde      ",
                                                 "Naranja    ", 
                                                 "Rojo       ", 
                                                 "Azul       ", 
                                                 "Amarillo   "];
                                                 
  % Nombre de cada país
array[1..numPaises] of string: nombrePaises = ["Argentina           ", 
                                               "Bolivia             ", 
                                               "Brasil              ", 
                                               "Chile               ", 
                                               "Colombia            ", 
                                               "Ecuador             ", 
                                               "Guayana Francesa    ", 
                                               "Guyana              ", 
                                               "Paraguay            ", 
                                               "Perú                ", 
                                               "Surinam             ", 
                                               "Uruguay             ", 
                                               "Venezuela           "];

  % Posiciones del vector mapaPaises en las que se encuentra cada país
int: Argentina = 1;
int: Bolivia = 2;
int: Brasil = 3;
int: Chile = 4;
int: Colombia = 5;
int: Ecuador = 6;
int: Guayana_Francesa = 7;
int: Guyana = 8;
int: Paraguay = 9;
int: Peru = 10;
int: Surinam = 11;
int: Uruguay = 12;
int: Venezuela = 13;

  % Vecinos de cada país
array[1..5] of int: vecinosArgentina = [Bolivia, Brasil, Chile, Paraguay, Uruguay];
array[1..5] of int: vecinosBolivia = [Argentina, Brasil, Chile, Paraguay, Peru];
array[1..10] of int: vecinosBrasil = [Argentina, Bolivia, Colombia, Guayana_Francesa, Guyana,Paraguay, Peru, Surinam, Uruguay, Venezuela];
array[1..3] of int: vecinosChile = [Argentina, Bolivia, Peru];
array[1..4] of int: vecinosColombia = [Brasil, Ecuador, Peru, Venezuela];
array[1..2] of int: vecinosEcuador = [Colombia, Peru];
array[1..2] of int: vecinosGuayanaFrancesa = [Brasil, Surinam];
array[1..3] of int: vecinosGuyana = [Brasil, Surinam, Venezuela];
array[1..4] of int: vecinosParaguay = [Argentina, Bolivia, Brasil, Uruguay];
array[1..5] of int: vecinosPeru = [Bolivia, Brasil, Chile, Colombia, Ecuador];
array[1..3] of int: vecinosSurinam = [Brasil, Guayana_Francesa, Guyana];
array[1..3] of int: vecinosUruguay = [Argentina, Brasil, Paraguay];
array[1..3] of int: vecinosVenezuela = [Brasil, Colombia, Guyana];


% Definición de restricciones:

  % Comprobar que ningún país tiene el mismo color que alguno de sus vecinos
    % Vecinos de Argentina
constraint 
forall(i in 1..5)(
  mapaPaises[Argentina] != mapaPaises[vecinosArgentina[i]]
);
    % Vecinos de Bolivia
constraint 
forall(i in 1..5)(
  mapaPaises[Bolivia] != mapaPaises[vecinosBolivia[i]]
);
    % Vecinos de Brasil
constraint 
forall(i in 1..10)(
  mapaPaises[Brasil] != mapaPaises[vecinosBrasil[i]]
);
    % Vecinos de Chile
constraint 
forall(i in 1..3)(
  mapaPaises[Chile] != mapaPaises[vecinosChile[i]]
);
    % Vecinos de Colombia
constraint 
forall(i in 1..4)(
  mapaPaises[Colombia] != mapaPaises[vecinosColombia[i]]
);
    % Vecinos de Ecuador
constraint 
forall(i in 1..2)(
  mapaPaises[Ecuador] != mapaPaises[vecinosEcuador[i]]
);
    % Vecinos de Guayana Francesa
constraint 
forall(i in 1..2)(
  mapaPaises[Guayana_Francesa] != mapaPaises[vecinosGuayanaFrancesa[i]]
);
    % Vecinos de Guyana
constraint 
forall(i in 1..3)(
  mapaPaises[Guyana] != mapaPaises[vecinosGuyana[i]]
);
    % Vecinos de Paraguay
constraint 
forall(i in 1..4)(
  mapaPaises[Paraguay] != mapaPaises[vecinosParaguay[i]]
);
    % Vecinos de Perú
constraint 
forall(i in 1..5)(
  mapaPaises[Peru] != mapaPaises[vecinosPeru[i]]
);
    % Vecinos de Surinam
constraint 
forall(i in 1..3)(
  mapaPaises[Surinam] != mapaPaises[vecinosSurinam[i]]
);
    % Vecinos de Uruguay
constraint 
forall(i in 1..3)(
  mapaPaises[Uruguay] != mapaPaises[vecinosUruguay[i]]
);
    % Vecinos de Venezuela
constraint 
forall(i in 1..3)(
  mapaPaises[Venezuela] != mapaPaises[vecinosVenezuela[i]]
);

  % Calcular coste total
constraint costeTotal == sum(i in 1..numPaises)(precioColores[mapaPaises[i]]);


% Indica que es un COP
solve minimize costeTotal;


% Salida por pantalla
output ["total: \(costeTotal)\n\n"] ++
       ["\(nombrePaises[i]): \(nombreColores[fix(mapaPaises[i])])  \(precioColores[mapaPaises[i]])\n" | i in 1..numPaises];
