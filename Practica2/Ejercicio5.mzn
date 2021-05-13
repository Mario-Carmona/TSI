% Ejercicio 5

include "globals.mzn";

% Definición de variables:

  % Número de asignaturas
int: numAsignaturas = 3;

  % Número de grupos
int: numGrupos = 4;

  % Número de aulas
int: numAulas = 3;

  % Número de tramos
int: numTramos = 4;

  % Variables que indican que número identifica a cada profesor
int: profesor1 = 1;
int: profesor2 = 2;
int: profesor3 = 3;
int: profesor4 = 4;

  % Variables que indican que número identifica a cada asignatura
int: IA = 1;
int: TSI = 2;
int: FBD = 3;

  % Variables que indican que número identifica a cada grupo de las distintas asignaturas
  % Un grupo se codifica como la codificación de la asignatura por 10 más el número del grupo al que pertenece. Ej: IA_G1 = 1*10+1
  % Para obtener la asignatura se divide por 10 y para obtener el grupo se hace modulo de 10
int: IA_G1 = 11;
int: IA_G2 = 12;
int: IA_G3 = 13;
int: IA_G4 = 14;
int: TSI_G1 = 21;
int: TSI_G2 = 22;
int: TSI_G3 = 23;
int: TSI_G4 = 24;
int: FBD_G1 = 31;
int: FBD_G2 = 32;
int: FBD_G3 = 33;
int: FBD_G4 = 34;

  % Nombre de los distintos tramos
array[1..numTramos] of string: tramos = [" 9 - 10","10 - 11","11 - 12","12 - 13"];

  % Nombre de los grupos
array[1..numAsignaturas,1..numGrupos] of string: grupos = [|" IA-G1 (P1)", " IA-G2 (P2)", " IA-G3 (P3)", " IA-G4 (P4)",
                                                           |"TSI-G1 (P3)", "TSI-G2 (P3)", "TSI-G3 (P4)", "TSI-G4 (P3)",
                                                           |"FBD-G1 (P1)", "FBD-G2 (P1)", "FBD-G3 (P1)", "FBD-G4 (P2)"|];

  % Matriz que indica que profesor imparte cada uno de los grupos de cada asignatura
array[1..numAsignaturas,1..numGrupos] of int: profAsig = [|1,2,3,4,
                                                          |3,3,4,3,
                                                          |1,1,1,2|];

  % Matriz que representa la asignación de grupos en los distintos tramos y aulas
array[1..numTramos,1..numAulas] of var {IA_G1, IA_G2, IA_G3, IA_G4, TSI_G1, TSI_G2, TSI_G3, TSI_G4, FBD_G1, FBD_G2, FBD_G3, FBD_G4}: horario;


% Definición de restricciones:

  % Comprobar que no hay grupos repetidos en el horario
constraint all_different(horario);

  % No puede impartir clase el mismo profesor en varias aulas en el mismo tramo horario
constraint 
forall(i in 1..numTramos)(
  all_different(j in 1..numAulas)(
    profAsig[horario[i,j] div 10, horario[i,j] mod 10]
  )
);

  % No puede dar clase en varias aulas el mismo grupo en un mismo tramo horario
constraint 
forall(i in 1..numTramos)(
  all_different(j in 1..numAulas)(
    horario[i,j] mod 10
  )
);

  % El profesor2 sólo puede dar clase en el aula 2
constraint 
forall(i in {1,3}, j in 1..numTramos)(
  profAsig[horario[j,i] div 10,horario[j,i] mod 10] != profesor2
);

  % El profesor4 sólo puede dar clase en el aula 3
constraint 
forall(i in {1,2}, j in 1..numTramos)(
  profAsig[horario[j,i] div 10,horario[j,i] mod 10] != profesor4
);

  % TSI no se puede impartir en el aula 1
constraint 
forall(i in 1..numTramos)(
  (horario[i,1] div 10) != TSI
);

  % IA-G1 debe impartirse en el primer tramo horario
constraint 
forall(i in 2..numTramos, j in 1..numAulas)(
  horario[i,j] != IA_G1
);

  % IA-G3 debe impartirse en el tercer tramo horario
constraint 
forall(i in {1,2,4}, j in 1..numAulas)(
  horario[i,j] != IA_G3
);

  % IA-G3 debe impartirse en el aula 1
constraint 
forall(i in 1..numTramos, j in 2..numAulas)(
  horario[i,j] != IA_G3
);

  % El profesor2 sólo da clase en el primer y segundo tramo horario
constraint 
forall(i in 3..numTramos, j in 1..numAulas)(
  profAsig[horario[i,j] div 10,horario[i,j] mod 10] != profesor2
);


% Indica que es un CSP
solve satisfy;


% Salida por pantalla

output ["                Aula1         Aula2         Aula3\n"] ++
       ["\(tramos[1]) ->"]++
       [show(grupos[fix(horario[1,i] div 10),fix(horario[1,i] mod 10)])++" " | i in 1..numAulas]++
       ["\n"]++
       ["\(tramos[2]) ->"]++
       [show(grupos[fix(horario[2,i] div 10),fix(horario[2,i] mod 10)])++" " | i in 1..numAulas]++
       ["\n"]++
       ["\(tramos[3]) ->"]++
       [show(grupos[fix(horario[3,i] div 10),fix(horario[3,i] mod 10)])++" " | i in 1..numAulas]++
       ["\n"]++
       ["\(tramos[4]) ->"]++
       [show(grupos[fix(horario[4,i] div 10),fix(horario[4,i] mod 10)])++" " | i in 1..numAulas];
