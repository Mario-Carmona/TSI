package TSI;

public class CasillaRango implements Comparable<CasillaRango> {

	Pos2D pos;
	CasillaCalor casillaCalor;
	int rango;
	
	public CasillaRango(int rango, CasillaCalor casillaCalor, Pos2D pos) {
		this.rango = rango;
		this.casillaCalor = casillaCalor;
		this.pos = pos;
	}
	
	@Override
	public int compareTo(CasillaRango arg0) {
		if(this.casillaCalor.calorEnemigosTotal != 0 &&
		   arg0.casillaCalor.calorEnemigosTotal == 0) {
			return 1;
		}
		else if(this.casillaCalor.calorEnemigosTotal == 0 &&
		   arg0.casillaCalor.calorEnemigosTotal != 0) {
			return -1;
		}
		else {
			double resultado = arg0.rango - this.rango;
			if(resultado < 0) {
				return -1;
			}
			else if(resultado > 0) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final CasillaRango other = (CasillaRango) obj;
        if (!this.pos.equals(other.pos)) { return false; }
        return true;
    }
}
