package TSI;

public class CasillaCalor {
	
	int calorObstaculo;
	int [] calorEnemigos;
	int calorEnemigosTotal;
	
	public CasillaCalor(int numEnemigos) {
		calorObstaculo = 0;
		calorEnemigos = new int[numEnemigos];
		for(int i = 0 ; i < numEnemigos; ++i) {
			calorEnemigos[i] = 0;
		}
		calorEnemigosTotal = 0;
	}
	
	int obtenerCalorTotal() {
		return calorObstaculo + calorEnemigosTotal;
	}
}
