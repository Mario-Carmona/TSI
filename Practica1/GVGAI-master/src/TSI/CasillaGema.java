package TSI;

public class CasillaGema implements Comparable<CasillaGema> {

	Pos2D pos;
	CasillaCalor casillaCalor;
	
	public CasillaGema(CasillaCalor casillaCalor, Pos2D pos) {
		this.casillaCalor = casillaCalor;
		this.pos = pos;
	}
	
	@Override
	public int compareTo(CasillaGema arg0) {
		if(this.casillaCalor.calorEnemigosTotal != 0 &&
		   arg0.casillaCalor.calorEnemigosTotal == 0) {
			return 1;
		}
		else if(this.casillaCalor.calorEnemigosTotal == 0 &&
		   arg0.casillaCalor.calorEnemigosTotal != 0) {
			return -1;
		}
		else {
			return 1;
		}
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final CasillaGema other = (CasillaGema) obj;
        if (!this.pos.equals(other.pos)) { return false; }
        return true;
    }
}
