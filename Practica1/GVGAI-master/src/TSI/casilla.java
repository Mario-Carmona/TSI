package TSI;

import java.util.ArrayList;

public class casilla {

	ArrayList<nodo> posiciones;
	Boolean obstaculo;
	
	public casilla() {
		obstaculo = false;
		for(int i = 0; i < 4; ++i) {
			posiciones.add(new nodo());
		}
	}
	
	public void setObstaculo(Boolean newValue) {
		obstaculo = newValue;
	}
}
