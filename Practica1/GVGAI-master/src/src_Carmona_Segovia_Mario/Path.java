package src_Carmona_Segovia_Mario;

import java.util.ArrayList;

import ontology.Types;

/*
 * Representa un camino desde un origen hasta un destino
 */
public class Path implements Comparable<Path> {
	
	// Posición de origen con su orientación
	Pos3D posOrigen;
	// posición de destino con su orientación
	Pos3D posDestino;
	// Lista de acciones para realizar el camino
	ArrayList<Types.ACTIONS> acciones;
	// Indicador de la acción a realizar en cada momento
	int contador;
	
	public Path(Pos3D posOrigen, Pos3D posDestino) {
		contador = 0;
		this.posDestino = posDestino;
		this.posOrigen = posOrigen;
		acciones = new ArrayList<Types.ACTIONS>();
	}
	
	public Path(ArrayList<Types.ACTIONS> acciones, Pos3D posOrigen, Pos3D posDestino) {
		contador = 0;
		this.posDestino = posDestino;
		this.posOrigen = posOrigen;
		this.acciones = acciones;
	}
	
	@Override
	public int compareTo(Path arg0) {
		return this.contador - arg0.contador;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Path other = (Path) obj;
        if (!this.posOrigen.equals(other.posOrigen)) { return false; }
        if (!this.posDestino.equals(other.posDestino)) { return false; }
        return true;
    }
	
	/*
	 * Método que devuelve la acción a realizar y aumenta el contador ó
	 * si el contador ya ha llegado al final devuelve la acción nil
	 */
	public Types.ACTIONS siguienteAccion() {
		
		if(contador < acciones.size()) {
			Types.ACTIONS accion = acciones.get(contador);
			++contador;
			return accion;
		}
		else {
			return Types.ACTIONS.ACTION_NIL;
		}
	}
	
	/*
	 * Añadir acción al inicio del camino
	 */
	public void addAccionInicio(Types.ACTIONS accion) {
		acciones.add(0, accion);
	}
	
	/*
	 * Añadir acción al final del camino
	 */
	public void addAccionFin(Types.ACTIONS accion) {
		acciones.add(acciones.size(), accion);
	}
}
