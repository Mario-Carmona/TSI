package TSI;

/*
 * Representa una posición en el mapa más su orientación
 */
public class Pos3D implements Comparable<Pos3D> {
	
	// Posición en el mapa
	Pos2D xy;
	// Orientación
	int ori;
	
	public Pos3D() {
		xy = new Pos2D();
		ori = 0;
	}
	
	public Pos3D(int x, int y, int ori) {
		this.xy = new Pos2D(x, y);
		this.ori = ori;
	}
	
	public Pos3D(Pos2D xy, int ori) {
		this.xy = new Pos2D(xy);
		this.ori = ori;
	}
	
	public Pos3D(Pos3D otro) {
		this.xy = new Pos2D(otro.xy);
		this.ori = otro.ori;
	}

	@Override
	public int compareTo(Pos3D arg0) {
		return this.xy.compareTo(arg0.xy);
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Pos3D other = (Pos3D) obj;
        if (!this.xy.equals(other.xy)) { return false; }
        if (this.ori != other.ori) { return false; }
        return true;
    }
}