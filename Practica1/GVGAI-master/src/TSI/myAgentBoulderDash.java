package TSI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import TSI.casilla;


public class myAgentBoulderDash extends AbstractPlayer {

	Vector2d fescala;
	ArrayList<ArrayList<casilla>> matrizNodos;
	int indicePlan;
	Map<Vector<Vector2d>, path> pathDisponibles;
	
	
	// Varibles de control
	Boolean gemasEncontradas;
	int numGemasEncontrada;
	int numGemasNecesarias;
	
	public myAgentBoulderDash(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		// Variables de control
		if(stateObs.getResourcesPositions().length == 0) {
			gemasEncontradas = true;
			numGemasNecesarias = 0;
		}
		else {
			gemasEncontradas = false;
			numGemasEncontrada = 0;
			numGemasNecesarias = 9;
		}
		
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
	
        // Creación de la matriz de nodos
        int tamMapa = stateObs.getObservationGrid()[0].length;
        
        matrizNodos = new ArrayList<ArrayList<casilla>>();
        for(int i = 0; i < tamMapa; ++i) {
        	matrizNodos.add(new ArrayList<casilla>());
        	for(int j = 0; j < tamMapa; ++j) {
        		matrizNodos.get(i).add(new casilla());
        	}
        }
        
        // Colocar los obstaculos
        for(ArrayList<Observation> i: stateObs.getImmovablePositions()) {
        	int x = (int)(i.get(0).position.x / fescala.x);
            int y = (int)(i.get(0).position.y / fescala.y);
            matrizNodos.get(x).get(y).setObstaculo(true);
        }
        
        
	}
	
	public void init(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		Boolean terminado = false;
		
		while(!terminado) {
			
			
			
		}
	}
	
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		
		
		
		
		return Types.ACTIONS.ACTION_NIL;
	}
	
	private path pathfinding_A_star() {
		
	}
}
