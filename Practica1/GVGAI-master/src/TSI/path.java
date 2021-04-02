package TSI;

import java.util.ArrayList;
import java.util.List;

import ontology.Types;
import ontology.Types.ACTIONS;

public class path {
	
	int oriOrigen;
	int oriDestino;
	ArrayList<Types.ACTIONS> acciones;
	int contador;
	
	public path() {
		contador = 0;
		oriDestino = 0;
		oriOrigen = 0;
		acciones = new ArrayList<Types.ACTIONS>();
	}
	
	public path(ArrayList<Types.ACTIONS> newAcciones, int newOriOrigen, int newOriDestino) {
		contador = 0;
		oriDestino = newOriDestino;
		oriOrigen = newOriOrigen;
		acciones = newAcciones;
	}
	
	public Types.ACTIONS siguienteAccion() {
		
		if(contador < acciones.size()) {
			++contador;
			return acciones.get(contador-1);
		}
		else {
			return Types.ACTIONS.ACTION_NIL;
		}
	}
	
	public int getOriOrigen() {
		return oriOrigen;
	}
	
	public int getOriDestino() {
		return oriDestino;
	}
	
	public void setPath(ArrayList<Types.ACTIONS> newPath) {
		acciones = newPath;
	}
	
	public void addAccionInicio(Types.ACTIONS accion) {
		acciones.add(0, accion);
	}
	
	public void addAccionFin(Types.ACTIONS accion) {
		acciones.add(acciones.size(), accion);
	}
}
