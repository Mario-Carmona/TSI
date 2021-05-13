% Ejercicio 4

include "globals.mzn";

% Definición de variables:

  % Número de días en una semana
int: numDias = 7;

  % Número de afirmaciones
int: numAfirmaciones = 7;

  % Enumarado de los distintos dias de la semana
enum dias ={Lunes, Martes, Miercoles, Jueves, Viernes, Sabado, Domingo};

  % Nombre de cada día de la semana
array[1..numDias] of string: nombreDias = ["Lunes", 
                                           "Martes", 
                                           "Miercoles", 
                                           "Jueves", 
                                           "Viernes", 
                                           "Sábado", 
                                           "Domingo"];
                          
  % Nombres de los emisores de las afirmaciones                                     
array[1..numAfirmaciones] of string: emisorAfirmaciones = ["Juan", 
                                                           "María", 
                                                           "Carmen", 
                                                           "Francisco", 
                                                           "Marta", 
                                                           "Olga", 
                                                           "Pepe"];
                                                           
  % Vector que indica que afirmaciones son correctas
array[1..numAfirmaciones] of var bool: afirmacionesCorrectas;

  % Variable que indica en que día nos encontramos hoy en base a las afirmaciones
var dias: hoy;


% Definición de restricciones:

  % Primera afirmación
constraint hoy == Martes -> afirmacionesCorrectas[1] == true;
  % Segunda afirmación
constraint enum_next(dias,hoy) == Martes -> afirmacionesCorrectas[2] == true;
  % Tercera afirmación
constraint enum_prev(dias,hoy) == Martes -> afirmacionesCorrectas[3] == true;
  % Cuarta afirmación
constraint enum_prev(dias,hoy) == Sabado -> afirmacionesCorrectas[4] == true;
  % Quinta afirmación
constraint hoy == Jueves \/ hoy == Viernes -> afirmacionesCorrectas[5] == true;
  % Sexta afirmación
constraint hoy == Domingo -> afirmacionesCorrectas[6] == true;
  % Septima afirmación
constraint hoy != Domingo -> afirmacionesCorrectas[7] == true;
  % Sólo una afirmación es correcta
constraint count(afirmacionesCorrectas, true) == 1;


% Indica que es un CSP
solve satisfy;


% Salida por pantalla
output [if fix(afirmacionesCorrectas[i])==true then "hoy: \"\(hoy)\"\nemisor: \(emisorAfirmaciones[i])" else "" endif | i in 1..numAfirmaciones];
