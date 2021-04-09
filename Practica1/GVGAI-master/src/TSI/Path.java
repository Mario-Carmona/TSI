package TSI;

import java.util.ArrayList;

import ontology.Types;

public class Path implements Comparable<Path> {
	
	Pos3D posOrigen;
	Pos3D posDestino;
	ArrayList<Types.ACTIONS> acciones;
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
	
	public Pos3D getPosOrigen() {
		return posOrigen;
	}
	
	public Pos3D getPosDestino() {
		return posDestino;
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
