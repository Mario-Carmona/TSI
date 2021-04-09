package TSI;


import java.util.HashSet;
import java.util.Set;

public class NodoGreedy implements Comparable<NodoGreedy> {
	
	int id;
	Pos2D pos;
	int coste;
	NodoGreedy padre;
	int gemasEscogidas;
	Set<Integer> gemasRestantes;
	
	public NodoGreedy(int x, int y, int id) {
		this.id = id;
		this.pos = new Pos2D(x,y);
		this.coste = 0;
		this.padre = null;
		this.gemasEscogidas = 0;
		this.gemasRestantes = null;
	}
	
	public NodoGreedy(Pos2D pos, int id) {
		this.id = id;
		this.pos = new Pos2D(pos);
		this.coste = 0;
		this.padre = null;
		this.gemasEscogidas = 0;
		this.gemasRestantes = null;
	}

	@Override
	public int compareTo(NodoGreedy arg0) {
		return this.coste - arg0.coste;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final NodoGreedy other = (NodoGreedy) obj;
        if (!this.pos.equals(other.pos)) { return false; }
        if (!this.padre.equals(other.padre)) { return false; }
        return true;
    }
}
