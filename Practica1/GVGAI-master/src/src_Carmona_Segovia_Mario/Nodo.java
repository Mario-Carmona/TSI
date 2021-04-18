package src_Carmona_Segovia_Mario;

import java.util.ArrayList;

import ontology.Types;

/*
 * Representa una orientación de una casilla del mapa
 */
public class Nodo implements Comparable<Nodo> {
	
	// Posición de la casilla más orientación
	Pos3D pos;
	// Acción calculada en el A*
	ArrayList<Types.ACTIONS> plan;
	// Coste del camino hasta la casilla
	int coste_g;
	// Coste total del camino desde el origen al destino
	// pasando por esta casilla
	int coste_f;
	// Puntero al padre en el A*
	Nodo padre;
	
	public Nodo(int x, int y, int ori) {
		pos = new Pos3D(x,y,ori);
		plan = new ArrayList<Types.ACTIONS>();
		coste_g = 0;
		coste_f = 0;
		padre = null;
	}
	
	public Nodo(Pos2D pos, int ori) {
		this.pos = new Pos3D(pos,ori);
		plan = new ArrayList<Types.ACTIONS>();
		coste_g = 0;
		coste_f = 0;
		padre = null;
	}

	@Override
	public int compareTo(Nodo arg0) {
		int diferencia = this.coste_f - arg0.coste_f;
		if(diferencia == 0) {
			if(this.pos.xy.x == arg0.pos.xy.x) {
				if(this.pos.xy.y == arg0.pos.xy.y) {
					diferencia = 0;
				}
				else {
					diferencia = -1;
				}
			}
			else {
				diferencia = -1;
			}
			diferencia = -1; 
		}
		return diferencia;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Nodo other = (Nodo) obj;
        if (!this.pos.equals(other.pos)) { return false; }
        return true;
    }
}
