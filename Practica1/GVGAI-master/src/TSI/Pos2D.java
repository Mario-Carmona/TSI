package TSI;

import tools.Vector2d;

/*
 * Representa una posición en el mapa
 */
public class Pos2D implements Comparable<Pos2D> {

	// Valor del eje X
	int x;
	// Valor del eje Y
	int y;
	
	public Pos2D() {
		x = 0;
		y = 0;
	}
	
	public Pos2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Pos2D(Vector2d otro) {
		this.x = (int)otro.x;
		this.y = (int)otro.y;
	}
	
	public Pos2D(Pos2D otro) {
		this.x = otro.x;
		this.y = otro.y;
	}
	
	@Override
	public int compareTo(Pos2D arg0) {
		return (this.x + this.y) - (arg0.x + arg0.y);
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Pos2D other = (Pos2D) obj;
        if (this.x != other.x) { return false; }
        if (this.y != other.y) { return false; }
        return true;
    }
}
