package src_Carmona_Segovia_Mario;

/*
 * Representa el calor de un casilla
 */
public class CasillaCalor {
	
	// Calor producido por obstáculos
	int calorObstaculo;
	// Calor producido por cada enemigo
	int [] calorEnemigos;
	// Calor producido por todos los enemigos juntos
	int calorEnemigosTotal;
	
	public CasillaCalor(int numEnemigos) {
		calorObstaculo = 0;
		calorEnemigos = new int[numEnemigos];
		for(int i = 0 ; i < numEnemigos; ++i) {
			calorEnemigos[i] = 0;
		}
		calorEnemigosTotal = 0;
	}
	
	/*
	 * Devuelve el calor total de la casilla, que es sumando
	 * el calor de los obstáculos y los enemigos
	 */
	int obtenerCalorTotal() {
		return calorObstaculo + calorEnemigosTotal;
	}
}
