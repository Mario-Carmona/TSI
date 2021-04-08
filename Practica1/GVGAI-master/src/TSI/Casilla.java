package TSI;

import java.util.ArrayList;

public class Casilla {

	Pos2D pos;
	ArrayList<Nodo> orientacion;
	Boolean obstaculo;
	Boolean gema;
	
	public Casilla(int x, int y) {
		pos = new Pos2D(x, y);
		obstaculo = false;
		gema = false;
		orientacion = new ArrayList<Nodo>();
		for(int i = 0; i < 4; ++i) {
			orientacion.add(new Nodo(pos,i));
		}
	}
	
	public void setObstaculo(Boolean newValue) {
		obstaculo = newValue;
	}
	
	public void setGema(Boolean newValue) {
		gema = newValue;
	}
}
