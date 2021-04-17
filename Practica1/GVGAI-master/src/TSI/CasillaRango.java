package TSI;

/*
 * Representa el rango de una casilla del mapa
 */
public class CasillaRango implements Comparable<CasillaRango> {
	
	// Posición de la casilla
	Pos2D pos;
	// Calor de la casilla
	CasillaCalor casillaCalor;
	// Indica a que distancia está el obstáculo más cercano
	// para saber se que cantidad de casilla disponemos para
	// movernos alrededor de esta casilla, es una estimación
	// no tiene porque parecersa a la real
	int rango;
	
	public CasillaRango(int rango, CasillaCalor casillaCalor, Pos2D pos) {
		this.rango = rango;
		this.casillaCalor = casillaCalor;
		this.pos = pos;
	}
	
	@Override
	public int compareTo(CasillaRango arg0) {
		// Se orden teniendo primero las casillas sin presencia
		// de calor de los enemigos, dentro de cada grupo se ordena
		// de mayor a menor rango.
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
			else {
				return 1;
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
