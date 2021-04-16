package TSI;


public class NodoGreedy implements Comparable<NodoGreedy> {
	
	int id;
	Pos2D pos;
	
	public NodoGreedy(int x, int y, int id) {
		this.id = id;
		this.pos = new Pos2D(x,y);
	}
	
	public NodoGreedy(Pos2D pos, int id) {
		this.id = id;
		this.pos = new Pos2D(pos);
	}

	@Override
	public int compareTo(NodoGreedy arg0) {
		return this.id - arg0.id;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final NodoGreedy other = (NodoGreedy) obj;
        if (!this.pos.equals(other.pos)) { return false; }
        return true;
    }
}
