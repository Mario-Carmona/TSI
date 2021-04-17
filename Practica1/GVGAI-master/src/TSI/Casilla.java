package TSI;

import java.util.ArrayList;

/*
 * Representa una casilla del mapa
 */
public class Casilla {

	// Posición de la casilla
	Pos2D pos;
	// Lista de nodos con las posibles orientaciones
	// de la casilla
	ArrayList<Nodo> orientacion;
	// Indicador de obstaculo en la casilla
	Boolean obstaculo;
	// Indicar de gema en la casilla
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
