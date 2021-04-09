package TSI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;


public class myAgentBoulderDash extends AbstractPlayer {

	Vector2d fescala;
	ArrayList<ArrayList<Casilla>> matrizNodos;
	int indicePlan;
	ArrayList<Path> pathDisponibles;
	ArrayList<NodoGreedy> listaObjetivos;
	Set<Integer> posObjetivos;
	Pos2D portal;
	Pos3D avatar;
	int pathActivo;
	
	
	// Varibles de control
	int nivel;
	Boolean gemasEncontradas;
	int numGemasEncontradas;
	int numGemasNecesarias;
	
	public myAgentBoulderDash(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		listaObjetivos = new ArrayList<NodoGreedy>();
		posObjetivos = new HashSet<Integer>();
		pathActivo = 0;
		
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
		
        // Obtener posición del avatar
        obtenerAvatar(stateObs);
		
		// Creación de la matriz de nodos
		crearTablero(stateObs);
		
		// Marcar los obstaculos en el tablero
		marcarObstaculos(stateObs);
        
        
		// Variables de control
        Boolean hayGemas, hayEnemigos;
        
        if(stateObs.getResourcesPositions() == null) { hayGemas = false; }
        else { hayGemas = true; }
        
        if(stateObs.getNPCPositions() == null) { hayEnemigos = false; }
        else { hayEnemigos = true; }
        
		if(!hayGemas) {
			if(!hayEnemigos) {
				numGemasEncontradas = 0;
				numGemasNecesarias = 0;
				nivel = 1;
			}
			else {
				numGemasEncontradas = 0;
				numGemasNecesarias = 0;
				nivel = 3;
			}
		}
		else {
			if(!hayEnemigos) {
				gemasEncontradas = false;
				numGemasEncontradas = 0;
				numGemasNecesarias = 9;
				marcarGemas(stateObs);
				nivel = 2;
			}
			else {
				gemasEncontradas = false;
				numGemasEncontradas = 0;
				numGemasNecesarias = 9;
				marcarGemas(stateObs);
				nivel = 5;
			}
		}
		
		
		// Obtener coordenadas del portal
		obtenerPortal(stateObs);
        
        pathDisponibles = new ArrayList<Path>();
        
        // Cálculos de cada nivel
        switch (nivel) {
		case 1:
			pathDisponibles.add(pathfinding_A_star(avatar, portal));
			break;
		case 2:
			ArrayList<Pos2D> listaPosiciones = greedy_manhattan();
			
			Pos3D posOrigen;
	        
			posOrigen = new Pos3D(avatar);
			
			for(Pos2D objetivo : listaPosiciones) {
				pathDisponibles.add(pathfinding_A_star(posOrigen, objetivo));
				posOrigen = pathDisponibles.get(pathDisponibles.size()-1).posDestino;
			}
			break;
		}
	}
	
	public void init(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		
	}
	
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		Types.ACTIONS accion = Types.ACTIONS.ACTION_NIL;
		
		switch (nivel) {
		case 1:
			accion = deliberativo_simple();
			break;
		case 2:
			accion = deliberativo_compuesto();
			break;
		}
		
		return accion;
	}
	
	private int distanciaManhattan(Pos2D current, Pos2D destino) {
		return Math.abs(current.x - destino.x) + Math.abs(current.y - destino.y);
	}
	
	private Path pathfinding_A_star(Pos3D posOrigen, Pos2D posDestino) {
		PriorityQueue<Nodo> colaAbiertos = new PriorityQueue<Nodo>();
		Set<Nodo> listaCerrados = new HashSet<Nodo>();
		Set<Nodo> listaAbiertos = new HashSet<Nodo>();
		
		Nodo current = matrizNodos.get(posOrigen.xy.x).get(posOrigen.xy.y).orientacion.get(posOrigen.ori);
		current.coste_g = 0;
		current.plan.clear();
		current.plan.add(Types.ACTIONS.ACTION_NIL);
		
		//Nodo destino = matrizNodos.get(posDestino.xy.x).get(posDestino.xy.y).orientacion.get(0);
		
		colaAbiertos.offer(current);
		listaAbiertos.add(current);
		
		int incre_g[] = new int[4];
		int coste_h;
		
		while(!colaAbiertos.isEmpty() && !current.pos.xy.equals(posDestino)) {
			
			colaAbiertos.poll();
			listaAbiertos.remove(current);
			listaCerrados.add(current);
			
			// Fijar incremento del coste g según la orientación
			switch (current.pos.ori) {
			case 0:	// Arriba
				incre_g[0] = 1;
				incre_g[1] = 2;
				incre_g[2] = 2;
				incre_g[3] = 2;
				break;
			case 1:	// Derecha
				incre_g[0] = 2;
				incre_g[1] = 1;
				incre_g[2] = 2;
				incre_g[3] = 2;
				break;
			case 2:	// Abajo
				incre_g[0] = 2;
				incre_g[1] = 2;
				incre_g[2] = 1;
				incre_g[3] = 2;
				break;
			case 3:	// Izquierda
				incre_g[0] = 2;
				incre_g[1] = 2;
				incre_g[2] = 2;
				incre_g[3] = 1;
				break;
			}
			
			// Generar descendiente de ir hacia arriba
			Casilla casillaUp = matrizNodos.get(current.pos.xy.x).get(current.pos.xy.y-1);
			Nodo hijoUp = casillaUp.orientacion.get(0);
			
			if(!casillaUp.obstaculo) {
				if(!listaCerrados.contains(hijoUp)) {
					coste_h = distanciaManhattan(hijoUp.pos.xy, posDestino);
					int new_coste_g = current.coste_g + incre_g[0];
					if(listaAbiertos.contains(hijoUp)) {
						if(new_coste_g < hijoUp.coste_g) {
							hijoUp.padre = current;
							hijoUp.coste_f = new_coste_g + coste_h;
							hijoUp.coste_g = new_coste_g;
							hijoUp.plan.clear();
							if(current.pos.ori != 0) {
								hijoUp.plan.add(ACTIONS.ACTION_UP);
							}
							hijoUp.plan.add(ACTIONS.ACTION_UP);
						}
					}
					else {
						hijoUp.padre = current;
						hijoUp.coste_f = new_coste_g + coste_h;
						hijoUp.coste_g = new_coste_g;
						hijoUp.plan.clear();
						if(current.pos.ori != 0) {
							hijoUp.plan.add(ACTIONS.ACTION_UP);
						}
						hijoUp.plan.add(ACTIONS.ACTION_UP);
						
						colaAbiertos.offer(hijoUp);
						listaAbiertos.add(hijoUp);
					}
				}
			}
			
			
			// Generar descendiente de ir hacia la derecha
			Casilla casillaRight = matrizNodos.get(current.pos.xy.x+1).get(current.pos.xy.y);
			Nodo hijoRight = casillaRight.orientacion.get(1);
			
			if(!casillaRight.obstaculo) {
				if(!listaCerrados.contains(hijoRight)) {
					coste_h = distanciaManhattan(hijoRight.pos.xy, posDestino);
					int new_coste_g = current.coste_g + incre_g[1];
					if(listaAbiertos.contains(hijoRight)) {
						if(new_coste_g < hijoRight.coste_g) {
							hijoRight.padre = current;
							hijoRight.coste_f = new_coste_g + coste_h;
							hijoRight.coste_g = new_coste_g;
							hijoRight.plan.clear();
							if(current.pos.ori != 1) {
								hijoRight.plan.add(ACTIONS.ACTION_RIGHT);
							}
							hijoRight.plan.add(ACTIONS.ACTION_RIGHT);
						}
					}
					else {
						hijoRight.padre = current;
						hijoRight.coste_f = new_coste_g + coste_h;
						hijoRight.coste_g = new_coste_g;
						hijoRight.plan.clear();
						if(current.pos.ori != 1) {
							hijoRight.plan.add(ACTIONS.ACTION_RIGHT);
						}
						hijoRight.plan.add(ACTIONS.ACTION_RIGHT);
						
						colaAbiertos.offer(hijoRight);
						listaAbiertos.add(hijoRight);
					}
				}
			}
			
			
			// Generar descendiente de ir hacia abajo
			Casilla casillaDown = matrizNodos.get(current.pos.xy.x).get(current.pos.xy.y+1);
			Nodo hijoDown = casillaDown.orientacion.get(2);
			
			if(!casillaDown.obstaculo) {
				if(!listaCerrados.contains(hijoDown)) {
					coste_h = distanciaManhattan(hijoDown.pos.xy, posDestino);
					int new_coste_g = current.coste_g + incre_g[2];
					if(listaAbiertos.contains(hijoDown)) {
						if(new_coste_g < hijoDown.coste_g) {
							hijoDown.padre = current;
							hijoDown.coste_f = new_coste_g + coste_h;
							hijoDown.coste_g = new_coste_g;
							hijoDown.plan.clear();
							if(current.pos.ori != 2) {
								hijoDown.plan.add(ACTIONS.ACTION_DOWN);
							}
							hijoDown.plan.add(ACTIONS.ACTION_DOWN);
						}
					}
					else {
						hijoDown.padre = current;
						hijoDown.coste_f = new_coste_g + coste_h;
						hijoDown.coste_g = new_coste_g;
						hijoDown.plan.clear();
						if(current.pos.ori != 2) {
							hijoDown.plan.add(ACTIONS.ACTION_DOWN);
						}
						hijoDown.plan.add(ACTIONS.ACTION_DOWN);
						
						colaAbiertos.offer(hijoDown);
						listaAbiertos.add(hijoDown);
					}
				}
			}
			
			
			// Generar descendiente de ir hacia la izquierda
			Casilla casillaLeft = matrizNodos.get(current.pos.xy.x-1).get(current.pos.xy.y);
			Nodo hijoLeft = casillaLeft.orientacion.get(3);
			
			if(!casillaLeft.obstaculo) {
				if(!listaCerrados.contains(hijoLeft)) {
					coste_h = distanciaManhattan(hijoLeft.pos.xy, posDestino);
					int new_coste_g = current.coste_g + incre_g[3];
					if(listaAbiertos.contains(hijoLeft)) {
						if(new_coste_g < hijoLeft.coste_g) {
							hijoLeft.padre = current;
							hijoLeft.coste_f = new_coste_g + coste_h;
							hijoLeft.coste_g = new_coste_g;
							hijoLeft.plan.clear();
							if(current.pos.ori != 3) {
								hijoLeft.plan.add(ACTIONS.ACTION_LEFT);
							}
							hijoLeft.plan.add(ACTIONS.ACTION_LEFT);
						}
					}
					else {
						hijoLeft.padre = current;
						hijoLeft.coste_f = new_coste_g + coste_h;
						hijoLeft.coste_g = new_coste_g;
						hijoLeft.plan.clear();
						if(current.pos.ori != 3) {
							hijoLeft.plan.add(ACTIONS.ACTION_LEFT);
						}
						hijoLeft.plan.add(ACTIONS.ACTION_LEFT);
						
						colaAbiertos.offer(hijoLeft);
						listaAbiertos.add(hijoLeft);
					}
				}
			}
			
			
			if(!colaAbiertos.isEmpty()) {
				current = colaAbiertos.peek();
			}
		}
		
		
		Path salida = null;

		if(current.pos.xy.equals(posDestino)) {
			salida = new Path(posOrigen, current.pos);
			
			Nodo iter = current;
			while(iter.plan.get(0) != ACTIONS.ACTION_NIL) {
				for(ACTIONS accion : iter.plan) {
					salida.addAccionInicio(accion);
				}
				iter = iter.padre;
			}
		}
		
		return salida;
	}
	
	ArrayList<Pos2D> greedy_manhattan() {
		PriorityQueue<NodoGreedy> colaAbiertos = new PriorityQueue<NodoGreedy>();
		
		Integer current = 0;
		NodoGreedy nodoCurrent = listaObjetivos.get(current);
		nodoCurrent.gemasRestantes = new HashSet<Integer>(posObjetivos);
		
		int id_portal = listaObjetivos.get(listaObjetivos.size()-1).id;
		nodoCurrent.gemasRestantes.remove(current);
		nodoCurrent.gemasRestantes.remove(id_portal);
		
		colaAbiertos.offer(nodoCurrent);
		
		while(!colaAbiertos.isEmpty() && nodoCurrent.gemasEscogidas != numGemasNecesarias + 1) {
			
			colaAbiertos.poll();
			
			if(nodoCurrent.gemasEscogidas < numGemasNecesarias) {
				for(Integer hijo : nodoCurrent.gemasRestantes) {
					NodoGreedy nodoHijo = listaObjetivos.get(hijo);
					
					int new_coste = nodoCurrent.coste + distanciaManhattan(nodoCurrent.pos, nodoHijo.pos);
					nodoHijo.padre = nodoCurrent;
					nodoHijo.coste = new_coste;
					nodoHijo.gemasEscogidas = nodoCurrent.gemasEscogidas + 1;
					nodoHijo.gemasRestantes = new HashSet<Integer>(nodoCurrent.gemasRestantes);
					nodoHijo.gemasRestantes.remove(hijo);
					
					colaAbiertos.offer(nodoHijo);
				}
			}
			else if(nodoCurrent.gemasEscogidas == numGemasNecesarias) {
				Integer hijo = id_portal;
				NodoGreedy nodoHijo = listaObjetivos.get(hijo);
				
				int new_coste = nodoCurrent.coste + distanciaManhattan(nodoCurrent.pos, nodoHijo.pos);
				nodoHijo.padre = nodoCurrent;
				nodoHijo.coste = new_coste;
				nodoHijo.gemasEscogidas = nodoCurrent.gemasEscogidas + 1;
				nodoHijo.gemasRestantes = new HashSet<Integer>(nodoCurrent.gemasRestantes);
				nodoHijo.gemasRestantes.remove(hijo);
				
				colaAbiertos.offer(nodoHijo);
			}
			
			if(!colaAbiertos.isEmpty()) {
				nodoCurrent = colaAbiertos.peek();
				current = nodoCurrent.id;
			}
		}
		
		ArrayList<Pos2D> salida = new ArrayList<Pos2D>();

		if(nodoCurrent.gemasEscogidas == numGemasNecesarias + 1) {
			NodoGreedy iter = nodoCurrent;
			while(iter.padre != null) {
				salida.add(0, iter.pos);
				iter = iter.padre;
			}
		}
		
		return salida;
	}
	
	private Types.ACTIONS deliberativo_simple() {
		Types.ACTIONS accion = pathDisponibles.get(0).siguienteAccion();
		return accion;
	}
	
	private Types.ACTIONS deliberativo_compuesto() {
		ACTIONS accion = pathDisponibles.get(pathActivo).siguienteAccion();
		
		if(accion == ACTIONS.ACTION_NIL) {
			pathActivo = pathActivo + 1;
			accion = pathDisponibles.get(pathActivo).siguienteAccion();
		}
		return accion;
	}
	
	private void obtenerPortal(StateObservation stateObs) {
		portal = new Pos2D((int)Math.floor(stateObs.getPortalsPositions()[0].get(0).position.x / fescala.x),
				           (int)Math.floor(stateObs.getPortalsPositions()[0].get(0).position.y / fescala.y));
		
		posObjetivos.add(listaObjetivos.size());
		listaObjetivos.add(new NodoGreedy(portal, listaObjetivos.size()));
	}	
	
	private int obtenerOriAvatar(Pos2D pos) {
		if(pos.y == -1) { return 0; }	// Arriba
		if(pos.x == 1) { return 1; }	// Derecha
		if(pos.y == 1) { return 2; }	// Abajo
		if(pos.x == -1) { return 3; }	// Izquierda
		
		return -1;
	}
	
	private void obtenerAvatar(StateObservation stateObs) {
		avatar = new Pos3D((int)Math.floor(stateObs.getAvatarPosition().x / fescala.x), 
						   (int)Math.floor(stateObs.getAvatarPosition().y / fescala.y),
						   obtenerOriAvatar(new Pos2D(stateObs.getAvatarOrientation())));
		
		posObjetivos.add(listaObjetivos.size());
		listaObjetivos.add(new NodoGreedy(avatar.xy, listaObjetivos.size()));
	}
	
	private void marcarObstaculos(StateObservation stateObs) {
		for(ArrayList<Observation> resource: stateObs.getImmovablePositions()) {
			for(Observation obstaculo : resource) {
				int x = (int)Math.floor(obstaculo.position.x / fescala.x);
	            int y = (int)Math.floor(obstaculo.position.y / fescala.y);
	            matrizNodos.get(x).get(y).setObstaculo(true);
			}
        }
	}
	
	private void marcarGemas(StateObservation stateObs) {
		for(ArrayList<Observation> resource : stateObs.getResourcesPositions()) {
			for(Observation gema : resource) {
				int x = (int)Math.floor(gema.position.x / fescala.x);
	            int y = (int)Math.floor(gema.position.y / fescala.y);
	            matrizNodos.get(x).get(y).setGema(true);
	            
	            posObjetivos.add(listaObjetivos.size());
	            Pos2D aux = new Pos2D(x,y);
	            listaObjetivos.add(new NodoGreedy(aux, listaObjetivos.size()));
			}
		}
	}
	
	private void crearTablero(StateObservation stateObs) {
		int tamMapaX = stateObs.getObservationGrid().length;
        int tamMapaY = stateObs.getObservationGrid()[0].length;
        
        matrizNodos = new ArrayList<ArrayList<Casilla>>();
        for(int x = 0; x < tamMapaX; ++x) {
        	matrizNodos.add(new ArrayList<Casilla>());
        	for(int y = 0; y < tamMapaY; ++y) {
        		matrizNodos.get(x).add(new Casilla(x,y));
        	}
        }
	}
}
