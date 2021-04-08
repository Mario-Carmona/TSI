package TSI;

import java.util.ArrayList;

import ontology.Types;

public class Nodo implements Comparable<Nodo> {
	
	Pos3D pos;
	ArrayList<Types.ACTIONS> plan;
	int coste_g;
	int coste_f;
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
		return this.coste_f - arg0.coste_f;
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
