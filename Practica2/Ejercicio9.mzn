% Ejercicio 9

include "globals.mzn";

% Definición de variables:

  % Número de pacientes
int: numPacientes = 20;

  % Número de posibles síntomas de cada paciente
int: numSintomas = 4;

  % Nombre de cada paciente
array[1..numPacientes] of string: nombres = ["Paciente01", "Paciente02", "Paciente03", "Paciente04", "Paciente05", "Paciente06", "Paciente07", "Paciente08", "Paciente09", "Paciente10", "Paciente11", "Paciente12", "Paciente13", "Paciente14", "Paciente15", "Paciente16", "Paciente17", "Paciente18", "Paciente19", "Paciente20",];

  % Nombre del diagnóstico de cada paciente
array[1..numPacientes] of string: diagnosticos = ["COVID", "COVID", "COVID", "COVID", "COVID", "COVID", "COVID", "COVID", "COVID", "COVID", "Gripe", "Gripe", "Gripe", "Gripe", "Gripe", "Gripe", "Alergias", "Alergias", "Alergias", "Alergias"];

  % Matriz que indica los síntomas de cada paciente
array[1..numPacientes,1..numSintomas] of 0..1: pacientes = [|0,0,1,1,
                                                            |1,1,1,0,
                                                            |1,1,0,1,
                                                            |1,0,0,1,
                                                            |1,1,0,1,
                                                            |0,1,1,1,
                                                            |1,0,0,0,
                                                            |1,1,0,0,
                                                            |0,1,0,0,
                                                            |0,0,1,1,
                                                            |1,0,0,1,
                                                            |1,0,0,1,
                                                            |0,1,1,0,
                                                            |1,1,0,0,
                                                            |1,0,1,0,
                                                            |1,0,0,0,
                                                            |1,0,0,1,
                                                            |0,1,1,0,
                                                            |0,0,1,0,
                                                            |1,0,1,0|];

  % Vector que indica los síntomas del nuevo paciente sin diagnosticar
array[1..numSintomas] of 0..1: nuevoPaciente = [0,1,1,1];

  % Indice del paciente con menor distancia al nuevo paciente
var 1..numPacientes: pacienteMinimo;

  % Distancia mínima con el nuevo paciente
var 0..numSintomas: distanciaMinima;

  % Vector de distancias entre cada paciente y el nuevo paciente
array[1..numPacientes] of var -numSintomas..numSintomas: distancias;


% Definición de restricciones:

  % Calcular las distancias entre pacientes
constraint 
forall(j in 1..numPacientes)(
  distancias[j] = sum(i in 1..numSintomas) ( (nuevoPaciente[i]-pacientes[j,i])*(nuevoPaciente[i]-pacientes[j,i]) )
  /\ 
  distancias[j] >= 0
);
  
  % Obtener el menor valor de distancia
constraint distanciaMinima == distancias[pacienteMinimo];
    
    
% Indica que es un COP
solve minimize distanciaMinima;


% Salida por pantalla

output ["Distancias:\n\n"]++
       [show(nombres[i])++" -> "++show(distancias[i])++"\n" | i in 1..numPacientes]++
       ["\n"]++
       ["Paciente más parecido -> "++show(pacienteMinimo)++"\n"]++
       ["Probable diagnostico -> "++show(diagnosticos[fix(pacienteMinimo)])];
