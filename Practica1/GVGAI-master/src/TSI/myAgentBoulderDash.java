package TSI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.print.attribute.standard.NumberOfDocuments;

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
	Pos2D portal;
	Pos3D avatar;
	int pathActivo;
	ArrayList<ArrayList<CasillaCalor>> mapaCalor;
	SortedSet<CasillaRango> listaRangoCasillas;
	ArrayList<Pos2D> listaEnemigos;
	ArrayList<Pos2D> listaObstaculos;
	int tamZonaCalor;
	Pos2D centroMapa;
	int distanciaGreedy[][];
	Set<NodoGreedy> listaObjetivos;
	
	
	// Varibles de control
	Nivel nivel;
	Boolean gemasEncontradas;
	int numGemasEncontradas;
	int numGemasNecesarias;
	
	Boolean irALugarSeguro;
	Boolean enLugarSeguro;
	
	public myAgentBoulderDash(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		listaObjetivos = new HashSet<NodoGreedy>();
		pathActivo = 0;
		
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
		
        
        obtenerEnemigos(stateObs);
        
		// Creación de la matriz de nodos y del mapa de calor
		crearTablero(stateObs);
		
		// Marcar los obstaculos en el tablero
		obtenerObstaculos(stateObs);
		marcarObstaculos();
        
        
		// Variables de control
        Boolean hayGemas, hayEnemigos;
        
        if(stateObs.getResourcesPositions() == null) { hayGemas = false; }
        else { hayGemas = true; }
        
        if(stateObs.getNPCPositions() == null) { hayEnemigos = false; }
        else { hayEnemigos = true; }
        
		if(!hayGemas) {
			if(!hayEnemigos) { nivel = Nivel.DELIB_SIMPLE; }
			else { nivel = Nivel.REACTIVO; }
		}
		else {
			if(!hayEnemigos) { nivel = Nivel.DELIB_COMPU; }
			else { nivel = Nivel.REACTIVO_DELIB; }
		}
        
		
        pathDisponibles = new ArrayList<Path>();
        
        // Cálculos de cada nivel
        switch (nivel) {
		case DELIB_SIMPLE:
			numGemasEncontradas = 0;
			numGemasNecesarias = 0;
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
	        // Obtener coordenadas del portal
			obtenerPortal(stateObs);
			
			pathDisponibles.add(pathfinding_A_star(avatar, portal));
			break;
		case DELIB_COMPU:
			gemasEncontradas = false;
			numGemasEncontradas = 0;
			numGemasNecesarias = 9;
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
			marcarGemas(stateObs);
			// Obtener coordenadas del portal
			obtenerPortal(stateObs);
			
			calcularDistanciaEntreObjetivos();
			
			ArrayList<Pos2D> listaPosiciones = greedy_manhattan();
			
			Pos3D posOrigen;
			posOrigen = new Pos3D(avatar);
			
			for(Pos2D objetivo : listaPosiciones) {
				pathDisponibles.add(pathfinding_A_star(posOrigen, objetivo));
				posOrigen = pathDisponibles.get(pathDisponibles.size()-1).posDestino;
			}
			break;
		case REACTIVO:
			numGemasEncontradas = 0;
			numGemasNecesarias = 0;
			
			irALugarSeguro = false;
			tamZonaCalor = 4;
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
			
			inicializarMapaCalor();
			inicializarListaRangoCasilla(stateObs);
			break;
		case REACTIVO_DELIB:
			gemasEncontradas = false;
			numGemasEncontradas = 0;
			numGemasNecesarias = 9;
			
			tamZonaCalor = 4;
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
			marcarGemas(stateObs);
			// Obtener coordenadas del portal
			obtenerPortal(stateObs);
			
			inicializarMapaCalor();
			break;
		}
	}
	
	public void init(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		
	}
	
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		Types.ACTIONS accion = Types.ACTIONS.ACTION_NIL;
		
		// Actualizar posición del avatar
		obtenerAvatar(stateObs);
		
		// Comprobar si hemos cogido una gema
		if(matrizNodos.get(avatar.xy.x).get(avatar.xy.y).gema) {
			// Eliminamos la gema del tablero, para evitar que sea contada
			// más veces al pasar por ella
			matrizNodos.get(avatar.xy.x).get(avatar.xy.y).gema = false;
			++numGemasEncontradas;
		}
		
		// Comprobar su ya he cogido todas las gemas necesarias
		if(numGemasEncontradas == numGemasNecesarias) {
			gemasEncontradas = true;
		}
		
		switch (nivel) {
		case DELIB_SIMPLE:
			accion = deliberativo_simple();
			break;
		case DELIB_COMPU:
			accion = deliberativo_compuesto();
			break;
		case REACTIVO:
			accion = reactivo(stateObs);
			break;
		case REACTIVO_DELIB:
			
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
		int gemas = 0;
		
		int valor_max = 3*matrizNodos.size();
		
		int min = valor_max;
		NodoGreedy nodoMin = null;
		int nodoElegido;
		
		Set<NodoGreedy> lista = new HashSet<NodoGreedy>(listaObjetivos);
		
		for(NodoGreedy objetivo : lista) {
			int distancia = distanciaManhattan(avatar.xy, objetivo.pos);
			if(distancia < min) {
				min = distancia;
				nodoMin = objetivo;
			}
		}
		
		ArrayList<Pos2D> salida = new ArrayList<Pos2D>();
		
		gemas += 1;
		salida.add(nodoMin.pos);
		nodoElegido = nodoMin.id;
		lista.remove(nodoMin);
		
		while(gemas < numGemasNecesarias) {
			min = valor_max;
			
			for(NodoGreedy objetivo : lista) {
				int distancia = distanciaGreedy[nodoElegido][objetivo.id];
				if(distancia < min) {
					min = distancia;
					nodoMin = objetivo;
				}
			}
			
			
			gemas += 1;
			salida.add(nodoMin.pos);
			nodoElegido = nodoMin.id;
			lista.remove(nodoMin);
		}
		
		// Añadir el portal
		salida.add(portal);
		
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
		
		if(gemasEncontradas) {
			if(pathDisponibles.get(pathActivo).posDestino.xy != portal) {
				// Si ya tengo todas las gemas y el camino actual no me lleva
				// al portal, calculo un nuevo camino
				
				pathDisponibles.clear();
				
				pathDisponibles.add(pathfinding_A_star(avatar, portal));
				pathActivo = 0;
				
				accion = pathDisponibles.get(pathActivo).siguienteAccion();
			}
		}
		
		return accion;
	}
	
	private Pos2D convertirPixelAGrid(Observation observacion) {
		return new Pos2D((int)Math.floor(observacion.position.x / fescala.x), 
						 (int)Math.floor(observacion.position.y / fescala.y));
	}
	
	private void obtenerPortal(StateObservation stateObs) {
		portal = new Pos2D((int)Math.floor(stateObs.getPortalsPositions()[0].get(0).position.x / fescala.x),
				           (int)Math.floor(stateObs.getPortalsPositions()[0].get(0).position.y / fescala.y));
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
	}
	
	private void obtenerObstaculos(StateObservation stateObs) {
		listaObstaculos = new ArrayList<Pos2D>();
		for(ArrayList<Observation> resource: stateObs.getImmovablePositions()) {
			for(Observation obstaculo : resource) {
				int x = (int)Math.floor(obstaculo.position.x / fescala.x);
	            int y = (int)Math.floor(obstaculo.position.y / fescala.y);
	            listaObstaculos.add(new Pos2D(x,y));
			}
        }
	}
	
	private void marcarObstaculos() {
		for(Pos2D obstaculo : listaObstaculos) {
            matrizNodos.get(obstaculo.x).get(obstaculo.y).setObstaculo(true);
		}
	}
	
	private void marcarGemas(StateObservation stateObs) {
		for(ArrayList<Observation> resource : stateObs.getResourcesPositions()) {
			for(Observation gema : resource) {
				int x = (int)Math.floor(gema.position.x / fescala.x);
	            int y = (int)Math.floor(gema.position.y / fescala.y);
	            matrizNodos.get(x).get(y).setGema(true);
	            
	            listaObjetivos.add(new NodoGreedy(new Pos2D(x,y), listaObjetivos.size()));
			}
		}
	}
	
	private void crearTablero(StateObservation stateObs) {
		int tamMapaX = stateObs.getObservationGrid().length;
        int tamMapaY = stateObs.getObservationGrid()[0].length;
        
        matrizNodos = new ArrayList<ArrayList<Casilla>>();
        mapaCalor = new ArrayList<ArrayList<CasillaCalor>>();
        for(int x = 0; x < tamMapaX; ++x) {
        	matrizNodos.add(new ArrayList<Casilla>());
        	mapaCalor.add(new ArrayList<CasillaCalor>());
        	for(int y = 0; y < tamMapaY; ++y) {
        		matrizNodos.get(x).add(new Casilla(x,y));
        		mapaCalor.get(x).add(new CasillaCalor(listaEnemigos.size()));
        	}
        }
        
        // Obtener centro del mapa
        
        int x = (int)matrizNodos.size() / 2;
        int y = (int)matrizNodos.get(0).size() / 2;
        centroMapa = new Pos2D(x, y);
	}
	
	private void inicializarMapaCalor() {
		// Obstaculos
		for(Pos2D obstaculo : listaObstaculos) {
			aplicarZonaCalorObs(obstaculo);
		}
		
		// Enemigos
		for(int i = 0; i < listaEnemigos.size(); ++i) {
			aplicarZonaCalorEne(listaEnemigos.get(i), i);
		}
	}
	
	private int obtenerRango(Pos2D posicion, Pos2D obstaculo) {
		int diferenciaX = Math.abs(posicion.x - obstaculo.x);
		int diferenciaY = Math.abs(posicion.y - obstaculo.y);
		
		return Math.max(diferenciaX, diferenciaY);
	}
	
	private int obtenerObstaculoMasCercano(Pos2D posicion) {
		int rangoMin = mapaCalor.size()+1;
		
		for(Pos2D obstaculo : listaObstaculos) {
			int nuevoRango = obtenerRango(posicion, obstaculo);
			if(nuevoRango < rangoMin) {
				rangoMin = nuevoRango;
			}
		}
		
		return rangoMin;
	}
	
	private void inicializarListaRangoCasilla(StateObservation stateObs) {
		listaRangoCasillas = new TreeSet<CasillaRango>();
		
		for(int x = 1; x < mapaCalor.size()-1; ++x) {
			for(int y = 1; y < mapaCalor.get(0).size()-1; ++y) {
				if(!matrizNodos.get(x).get(y).obstaculo) {
					Pos2D posicion = matrizNodos.get(x).get(y).pos;
					
					// Calcular rango disponible en cierta posición
					int rango = obtenerObstaculoMasCercano(posicion);
					
					listaRangoCasillas.add(new CasillaRango(rango, mapaCalor.get(x).get(y), new Pos2D(x,y)));
				}
			}
		}
	}
	
	private void obtenerEnemigos(StateObservation stateObs) {
		if(listaEnemigos == null) {
			listaEnemigos = new ArrayList<Pos2D>();
		}
		else {
			listaEnemigos.clear();
		}
		
		if(stateObs.getNPCPositions() != null) {
			for(ArrayList<Observation> lista : stateObs.getNPCPositions()) {
				for(Observation enemigo : lista) {
					int x = (int)Math.floor(enemigo.position.x / fescala.x);
		            int y = (int)Math.floor(enemigo.position.y / fescala.y);
		            listaEnemigos.add(new Pos2D(x,y));
				}
			}
		}
	}
	
	private void aplicarZonaCalorObs(Pos2D centro) {
		for(int x = -2; x < tamZonaCalor; ++x) {
			for(int y = -(tamZonaCalor-Math.abs(x)-1); y < tamZonaCalor-Math.abs(x); ++y) {
				int aux_x = centro.x+x;
				int aux_y = centro.y+y;
				if( (aux_x >= 0 && aux_x < mapaCalor.size()) && (aux_y >= 0 && aux_y < mapaCalor.get(0).size()) ) {
					int valor_actual = mapaCalor.get(aux_x).get(aux_y).calorObstaculo;
					int incre = tamZonaCalor - Math.abs(x) - Math.abs(y);
					mapaCalor.get(aux_x).get(aux_y).calorObstaculo = valor_actual + incre;
				}
			}
		}
	}
	
	private void aplicarZonaCalorEne(Pos2D centro, int id) {
		for(int x = -2; x < tamZonaCalor; ++x) {
			for(int y = -(tamZonaCalor-Math.abs(x)-1); y < tamZonaCalor-Math.abs(x); ++y) {
				int aux_x = centro.x+x;
				int aux_y = centro.y+y;
				if( (aux_x >= 0 && aux_x < mapaCalor.size()) && (aux_y >= 0 && aux_y < mapaCalor.get(0).size()) ) {
					int valor_actual = mapaCalor.get(aux_x).get(aux_y).calorEnemigos[id];
					int incre = tamZonaCalor - Math.abs(x) - Math.abs(y);
					mapaCalor.get(aux_x).get(aux_y).calorEnemigos[id] = valor_actual + incre;
					mapaCalor.get(aux_x).get(aux_y).calorEnemigosTotal += incre;
				}
			}
		}
	}
	
	private void desaplicarZonaCalorEne(Pos2D centro, int id) {
		for(int x = -2; x < tamZonaCalor; ++x) {
			for(int y = -(tamZonaCalor-Math.abs(x)-1); y < tamZonaCalor-Math.abs(x); ++y) {
				int aux_x = centro.x+x;
				int aux_y = centro.y+y;
				if( (aux_x >= 0 && aux_x < mapaCalor.size()) && (aux_y >= 0 && aux_y < mapaCalor.get(0).size()) ) {
					int valor_actual = mapaCalor.get(aux_x).get(aux_y).calorEnemigos[id];
					int incre = tamZonaCalor - Math.abs(x) - Math.abs(y);
					mapaCalor.get(aux_x).get(aux_y).calorEnemigos[id] = valor_actual - incre;
					mapaCalor.get(aux_x).get(aux_y).calorEnemigosTotal -= incre;
				}
			}
		}
	}
	
	private void actualizarEnemigos(StateObservation stateObs) {
		// Desaplicar zona de calor de las antiguas posiciones
		for(int i = 0; i < listaEnemigos.size(); ++i) {
			desaplicarZonaCalorEne(listaEnemigos.get(i), i);
		}
		
		// Actualizar sus posiciones
		obtenerEnemigos(stateObs);
		
		// Aplicar zonda de calor de las nuevas posiciones
		for(int i = 0; i < listaEnemigos.size(); ++i) {
			aplicarZonaCalorEne(listaEnemigos.get(i), i);
		}
	}
	
	private int obtenerCalorCercano(Pos2D posicion, int orientacion, Boolean calorTotal) {
		int calorMax = -1;
		
		for(int i = 0; i < 4; ++i) {
			if(orientacion != i) {
				int calorObtenido = 0;
				switch (orientacion) {
				case 0: // Arriba
					if(avatar.xy.y-1 >= 0) {
						if(calorTotal) {
							calorObtenido = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).obtenerCalorTotal();
						}
						else {
							calorObtenido = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).calorEnemigosTotal;
						}
						
					}
					break;
				case 1: // Derecha
					if(avatar.xy.x+1 < mapaCalor.size()) {
						if(calorTotal) {
							calorObtenido = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).obtenerCalorTotal();
						}
						else {
							calorObtenido = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).calorEnemigosTotal;
						}
					}
					break;
				case 2: // Abajo
					if(avatar.xy.y+1 < mapaCalor.get(0).size()) {
						if(calorTotal) {
							calorObtenido = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).obtenerCalorTotal();
						}
						else {
							calorObtenido = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).calorEnemigosTotal;
						}
					}
					break;
				case 3: // Izquierda
					if(avatar.xy.x-1 >= 0) {
						if(calorTotal) {
							calorObtenido = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).obtenerCalorTotal();
						}
						else {
							calorObtenido = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).calorEnemigosTotal;
						}
					}
					break;
				}
				
				if(calorMax < calorObtenido) {
					calorMax = calorObtenido;
				}
			}
		}
		
		// Se incrementa en una unidad si la dirección no es igual a la
		// orientación del avatar, para favorecer a la dirección que sigue
		// el avatar
		/*
		if(orientacion != avatar.ori) {
			++calorMax;
		}
		*/
		
		return calorMax;
	}
	
	private Object[] comprobarPerimetro() {
		
		int [][] calorTotal = new int[4][2];
		int [][] calorEnemigo = new int[4][2];
		
		for(int i = 0; i < 4; ++i) {
			switch (i) {
			case 0: // Arriba
				calorTotal[i][0] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).obtenerCalorTotal();
				calorTotal[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x, avatar.xy.y-1), i, true);
				
				calorEnemigo[i][0] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).calorEnemigosTotal;
				calorEnemigo[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x, avatar.xy.y-1), i, false);
				break;
			case 1: // Derecha
				calorTotal[i][0] = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).obtenerCalorTotal();
				calorTotal[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x+1, avatar.xy.y), i, true);
				
				calorEnemigo[i][0] = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).calorEnemigosTotal;
				calorEnemigo[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x+1, avatar.xy.y), i, false);
				break;
			case 2: // Abajo
				calorTotal[i][0] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).obtenerCalorTotal();
				calorTotal[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x, avatar.xy.y+1), i, true);
				
				calorEnemigo[i][0] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).calorEnemigosTotal;
				calorEnemigo[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x, avatar.xy.y+1), i, false);
				break;
			case 3: // Izquierda
				calorTotal[i][0] = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).obtenerCalorTotal();
				calorTotal[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x-1, avatar.xy.y), i, true);
				
				calorEnemigo[i][0] = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).calorEnemigosTotal;
				calorEnemigo[i][1] = obtenerCalorCercano(new Pos2D(avatar.xy.x-1, avatar.xy.y), i, false);
				break;
			}
			
			if(i != avatar.ori) {
				++calorTotal[i][0];
				++calorTotal[i][1];
			}
		}
		
		// Si no hay peligro
		
		Boolean sinPeligro = true;
		
		for(int i = 0; i < 4 && sinPeligro; ++i) {
			for(int j = 0; j < 2 && sinPeligro; ++j) {
				if(calorEnemigo[i][j] != 0) {
					sinPeligro = false;
				}
			}
		}
		
		if(sinPeligro) {
			Object[] salida = new Object[1];
			salida[0] = sinPeligro; // No hay peligro cercano
			return salida;
		}
		
		int calorMin = mapaCalor.size();
		ArrayList<Integer> elegidos = new ArrayList<Integer>();
		
		for(int i = 0; i < 4; ++i) {
			if(calorTotal[i][0] < calorMin) {
				calorMin = calorTotal[i][0];
				elegidos.clear();
			}
			
			if(calorTotal[i][0] == calorMin) {
				elegidos.add(i);
			}
		}
		
		
		Object[] salida = new Object[2];
		salida[0] = sinPeligro; // Hay peligro cercano
		switch (elegidos.size()) {
		case 1: // Huye en la única dirección sin peligro
			salida[1] = elegidos.get(0);
			break;
		case 2: // Huye a la dirección contraria de la que tiene más peligro cercano
		case 3:
		case 4:
			int calorMax = -1;
			int calorCercanoMax = -1;
			int elegido = -1;
			
			for(int i : elegidos) {
				int contrario = (i+2)%4;
				if(calorTotal[contrario][0] > calorMax) {
					calorMax = calorTotal[contrario][0];
					calorCercanoMax = calorTotal[contrario][1];
					elegido = i;
				}
				else if(calorTotal[contrario][0] == calorMax && calorTotal[contrario][1] > calorCercanoMax) {
					calorCercanoMax = calorTotal[contrario][1];
					elegido = i;
				}
			}
			
			salida[1] = elegido;
			break;
		}
		
		return salida;
	}
	
	private Pos2D seleccionarMejorPos() {
		Iterator<CasillaRango> iter = listaRangoCasillas.iterator();
		int rango = listaRangoCasillas.first().rango;
		Pos2D elegido = listaRangoCasillas.first().pos;
		int distancia = distanciaManhattan(avatar.xy, elegido);
		Boolean parar = false;
		
		iter.next();
		while(!parar) {
			CasillaRango casilla = iter.next();
			if(casilla.rango < rango) {
				parar = true;
			}
			else if(casilla.rango == rango) {
				int distAux = distanciaManhattan(avatar.xy, casilla.pos);
				if(distAux < distancia) {
					elegido = casilla.pos;
				}
			}
		}
		
		return elegido;
	}
	
	private Types.ACTIONS reactivo(StateObservation stateObs) {
		obtenerAvatar(stateObs);
		actualizarEnemigos(stateObs);
		
		// Comprobar situación alrededor del avatar
		Object[] resultado = comprobarPerimetro();
		
		Boolean sinPeligro = (Boolean)resultado[0];
		
		if(sinPeligro) {
			if(!irALugarSeguro) {
				Pos2D mejorPos = seleccionarMejorPos();
				
				pathDisponibles.clear();
				pathDisponibles.add(pathfinding_A_star(avatar, mejorPos));
				pathActivo = 0;
				irALugarSeguro = true;
				
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
			else {
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
		}
		else {
			irALugarSeguro = false;
			
			ACTIONS accion;
			
			switch ((int)resultado[1]) {
			case 0: // Arriba
				accion = ACTIONS.ACTION_UP;
				break;
			case 1: // Derecha
				accion = ACTIONS.ACTION_RIGHT;
				break;
			case 2: // Abajo
				accion = ACTIONS.ACTION_DOWN;
				break;
			case 3: // Izquierda
				accion = ACTIONS.ACTION_LEFT;
				break;
			default:
				accion = ACTIONS.ACTION_NIL;
				break;
			}
			
			return accion;
		}
		
		
		/*
		ACTIONS accion = pathDisponibles.get(pathActivo).siguienteAccion();
		
		if(accion == ACTIONS.ACTION_NIL) {
			obtenerAvatar(stateObs);
			actualizarEnemigos(stateObs);
			
			// Obtener el valor de las casillas a las que se puede mover el avatar
			ArrayList<Integer> listaCasillas =  posiblesCasillas();
			
			// Incrementar en uno el valor de las casillas en las que han falta dos acciones
			for(int i = 0; i < listaCasillas.size(); ++i) {
				if(avatar.ori != i) {
					listaCasillas.set(i, listaCasillas.get(i) + 1);
				}
			}
			
			// Obtener el mínimo valor de casilla
			int min = listaCasillas.get(0);
			ArrayList<Integer> direccion = new ArrayList<Integer>();
			direccion.add(0);
			for(int i = 1; i < listaCasillas.size(); ++i) {
				if(min > listaCasillas.get(i)) {
					min = listaCasillas.get(i);
					direccion.clear();
					direccion.add(i);
				}
				else if(min == listaCasillas.get(i)) {
					direccion.add(i);
				}
			}
			
			// Dirección definitiva
			int dire_defi = direccion.get(0);
			for(int i : direccion) {
				switch (i) {
				case 0:
					if(avatar.xy.y > centroMapa.y) {
						dire_defi = 0;
					}
					break;
				case 1:
					if(avatar.xy.x < centroMapa.x) {
						dire_defi = 1;
					}
					break;
				case 2:
					if(avatar.xy.y < centroMapa.y) {
						dire_defi = 2;
					}
					break;
				case 3:
					if(avatar.xy.x > centroMapa.x) {
						dire_defi = 3;
					}
					break;
				}
			}
			
			// Elegir plan
			pathDisponibles.clear();
			
			ACTIONS accionARealizar;
			
			switch (dire_defi) {
			case 0:
				accionARealizar = ACTIONS.ACTION_UP;
				break;
			case 1:
				accionARealizar = ACTIONS.ACTION_RIGHT;
				break;
			case 2:
				accionARealizar = ACTIONS.ACTION_DOWN;
				break;
			case 3:
				accionARealizar = ACTIONS.ACTION_LEFT;
				break;
			default:
				accionARealizar = ACTIONS.ACTION_NIL;
				break;
			}
			
			Path path = new Path(avatar, avatar);
			path.addAccionInicio(accionARealizar);
			if(dire_defi != avatar.ori) {
				path.addAccionInicio(accionARealizar);
			}
			pathDisponibles.add(path);
			
			accion = pathDisponibles.get(pathActivo).siguienteAccion();
		}
		
		return accion;
		*/
	}
	
	private void calcularDistanciaEntreObjetivos() {
		distanciaGreedy = new int[listaObjetivos.size()][listaObjetivos.size()];
		
		for(NodoGreedy objetivo1 : listaObjetivos) {
			for(NodoGreedy objetivo2 : listaObjetivos) {
				distanciaGreedy[objetivo1.id][objetivo2.id] = distanciaManhattan(objetivo1.pos, objetivo2.pos);
			}
		}
	}
}
