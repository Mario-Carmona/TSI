package TSI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;


public class myAgentBoulderDash extends AbstractPlayer {

	// Representación del mapa
	/*
	 * Vector que nos indica el paso de píxeles a grid
	 */
	Vector2d fescala;
	/*
	 * Matriz que representa el mapa como un grid, cada
	 * elementos es un objeto Casilla
	 */
	ArrayList<ArrayList<Casilla>> matrizNodos;
	
	// Planes
	/*
	 * Lista de los planes que el avatar tiene que realiza,
	 * o tiene pendientes de realizar
	 */
	ArrayList<Path> pathDisponibles;
	/*
	 * Variables que indica el índice del plan que se
	 * está ejecutando es este momento de entre los 
	 * elementos de la lista "pathDisponibles"
	 */
	int pathActivo;
	/*
	 * Matriz que contiene las distancias entre los distintos
	 * objetivos que hay en el mapa
	 */
	int[][] distanciaGreedy;
	
	// Objetos del mapa
	/*
	 * Posición del portal
	 */
	Pos2D portal;
	/*
	 * Posición del avatar
	 */
	Pos3D avatar;
	/*
	 * Lista con las posiciones de los distintos
	 * enemigos del mapa
	 */
	ArrayList<Pos2D> listaEnemigos;
	/*
	 * Lista con las posiciones de los distintos
	 * obstáculos del mapa
	 */
	ArrayList<Pos2D> listaObstaculos;
	/*
	 * Lista con las posiciones de las distintas
	 * gemas del mapa
	 */
	Set<CasillaGema> listaGemas;
	Set<NodoGreedy> listaObjetivos;
	
	// Recursos para el reactivo
	ArrayList<ArrayList<CasillaCalor>> mapaCalor;
	SortedSet<CasillaRango> listaRangoCasillas;
	
		// Fuerza del calor y su dispersión
	int tamZonaCalorEne;
	int fuerzaCalorEne;
	int tamZonaCalorObs;
	int fuerzaCalorObs;
		
		// Zonas
	int [][][] zonasEnemigas;
	int [][] zonaObs;
	
		// Variables de control
	Boolean irALugarSeguro;
	Boolean enLugarSeguro;
	Boolean irAObjetivo;
	
	// Varibles de control generales
	Nivel nivel;
	Boolean gemasEncontradas;
	int numGemasEncontradas;
	int numGemasNecesarias;
	
	
	
	public myAgentBoulderDash(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

		listaObjetivos = new HashSet<NodoGreedy>();
		listaGemas = new HashSet<CasillaGema>();
		pathDisponibles = new ArrayList<Path>();
		pathActivo = 0;
		
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
		
        // Obtener posiciones de los enemigos
        obtenerEnemigos(stateObs);
        
		// Creación de la matriz de nodos y del mapa de calor
		crearTablero(stateObs);
		
		// Obtener posiciones de los obstaculos
		obtenerObstaculos(stateObs);
		// Marcar los obstaculos en el tablero
		marcarObstaculos();
        
        
		// Obtener el nivel del mapa
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
        
		
        // Cálculos de cada nivel
        switch (nivel) {
		case DELIB_SIMPLE:
			gemasEncontradas = true;
			numGemasEncontradas = 0;
			numGemasNecesarias = 0;
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
	        // Obtener posición del portal
			obtenerPortal(stateObs);
			
			// Obtener el plan al portal
			pathDisponibles.add(pathfinding_A_star(avatar, portal));
			break;
		case DELIB_COMPU:
			gemasEncontradas = false;
			numGemasEncontradas = 0;
			numGemasNecesarias = 9;
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
	        // Obtener posición de las gemas y marcarlas en el mapa
			marcarGemas(stateObs);
			// Obtener posición del portal
			obtenerPortal(stateObs);
			
			// Calcular la distancia manhattan entre los objetivos
			calcularDistanciaEntreObjetivos();
			
			// Obtener la ruta de objetivos
			ArrayList<Pos2D> listaPosiciones = greedy_manhattan();
			
			// Obtener el plan para recorrer todos los objetivos
			Pos3D posOrigen = new Pos3D(avatar);
			
			for(Pos2D objetivo : listaPosiciones) {
				pathDisponibles.add(pathfinding_A_star(posOrigen, objetivo));
				posOrigen = pathDisponibles.get(pathDisponibles.size()-1).posDestino;
			}
			break;
		case REACTIVO:
			gemasEncontradas = true;
			numGemasEncontradas = 0;
			numGemasNecesarias = 0;
			
			irALugarSeguro = false;
			
			tamZonaCalorEne = 5;
			fuerzaCalorEne = 5;
			tamZonaCalorObs = 1;
			fuerzaCalorObs = 10;
			
			zonasEnemigas = new int[listaEnemigos.size()][2*tamZonaCalorEne - 1][2*tamZonaCalorEne - 1];
			zonaObs = new int[2*tamZonaCalorObs - 1][2*tamZonaCalorObs - 1];
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
			
			inicializarMapaCalor();
			inicializarListaRangoCasilla(stateObs);
			break;
		case REACTIVO_DELIB:
			gemasEncontradas = false;
			numGemasEncontradas = 0;
			numGemasNecesarias = 9;
			
			irAObjetivo = false;
			tamZonaCalorEne = 5;
			fuerzaCalorEne = 5;
			tamZonaCalorObs = 2;
			fuerzaCalorObs = 4;
			zonasEnemigas = new int[listaEnemigos.size()][2*tamZonaCalorEne - 1][2*tamZonaCalorEne - 1];
			zonaObs = new int[2*tamZonaCalorObs - 1][2*tamZonaCalorObs - 1];
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
			marcarGemas(stateObs);
			// Obtener coordenadas del portal
			obtenerPortal(stateObs);
			
			inicializarMapaCalor();
			break;
		}
	}
	
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		Types.ACTIONS accion = Types.ACTIONS.ACTION_NIL;
		
		obtenerAvatar(stateObs);
		
		// Comprobar si hemos cogido una gema
		if(matrizNodos.get(avatar.xy.x).get(avatar.xy.y).gema) {
			// Eliminamos la gema del tablero, para evitar que sea contada
			// más veces al pasar por ella
			matrizNodos.get(avatar.xy.x).get(avatar.xy.y).gema = false;
			irAObjetivo = false;
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
			accion = reactivo_deliberativo(stateObs);
			break;
		}
		
		return accion;
	}
	
	
	/****************************************************************************************/
	/****  General                                                                          */
	/****************************************************************************************/
	
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
					Pos2D pos = convertirPixelAGrid(enemigo);
		            listaEnemigos.add(pos);
				}
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
	}
	
	private void obtenerObstaculos(StateObservation stateObs) {
		listaObstaculos = new ArrayList<Pos2D>();
		for(ArrayList<Observation> resource: stateObs.getImmovablePositions()) {
			for(Observation obstaculo : resource) {
				Pos2D pos = convertirPixelAGrid(obstaculo);
	            listaObstaculos.add(pos);
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
				Pos2D pos = convertirPixelAGrid(gema);
	            matrizNodos.get(pos.x).get(pos.y).setGema(true);
	            
	            listaObjetivos.add(new NodoGreedy(pos, listaObjetivos.size()));
	            listaGemas.add(new CasillaGema(mapaCalor.get(pos.x).get(pos.y), pos));
			}
		}
	}
	
	private void obtenerAvatar(StateObservation stateObs) {
		avatar = new Pos3D((int)Math.floor(stateObs.getAvatarPosition().x / fescala.x), 
						   (int)Math.floor(stateObs.getAvatarPosition().y / fescala.y),
						   obtenerOriAvatar(new Pos2D(stateObs.getAvatarOrientation())));
	}
	
	private int obtenerOriAvatar(Pos2D pos) {
		if(pos.y == -1) { return 0; }	// Arriba
		if(pos.x == 1) { return 1; }	// Derecha
		if(pos.y == 1) { return 2; }	// Abajo
		if(pos.x == -1) { return 3; }	// Izquierda
		
		return -1;
	}
	
	private void obtenerPortal(StateObservation stateObs) {
		portal = convertirPixelAGrid(stateObs.getPortalsPositions()[0].get(0));
	}	
	
	private int distanciaManhattan(Pos2D current, Pos2D destino) {
		return Math.abs(current.x - destino.x) + Math.abs(current.y - destino.y);
	}
	
	private Pos2D convertirPixelAGrid(Observation observacion) {
		return new Pos2D((int)Math.floor(observacion.position.x / fescala.x), 
						 (int)Math.floor(observacion.position.y / fescala.y));
	}
	
	/****************************************************************************************/
	/****  Deliberativo                                                                     */
	/****************************************************************************************/
	
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
	
	private void calcularDistanciaEntreObjetivos() {
		distanciaGreedy = new int[listaObjetivos.size()][listaObjetivos.size()];
		
		for(NodoGreedy objetivo1 : listaObjetivos) {
			for(NodoGreedy objetivo2 : listaObjetivos) {
				distanciaGreedy[objetivo1.id][objetivo2.id] = distanciaManhattan(objetivo1.pos, objetivo2.pos);
			}
		}
	}
	
	private ArrayList<Pos2D> greedy_manhattan() {
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
	
	
	/****************************************************************************************/
	/****  Reactivo                                                                         */
	/****************************************************************************************/
	
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
	
	private int obtenerRango(Pos2D posicion, Pos2D obstaculo) {
		int diferenciaX = Math.abs(posicion.x - obstaculo.x);
		int diferenciaY = Math.abs(posicion.y - obstaculo.y);
		
		return Math.max(diferenciaX, diferenciaY);
	}
	
	private void aplicarZonaCalorObs(Pos2D centro) {
		// Crear zona de calor
		for(int i = 0; i < zonaObs.length; ++i) {
			for(int j = 0; j < zonaObs.length; ++j) {
				zonaObs[i][j] = 0;
			}
		}
		
		// Fijar calor del centro
		int x = tamZonaCalorObs-1;
		int y = tamZonaCalorObs-1;
		zonaObs[x][y] = fuerzaCalorObs;
		
		// Fijar calor de alrededor del centro mediante recursividad, se comprueba
		// que no se sale de los límites porque algunos de los elementos evaluados
		// son los muros del exterior del mapa
		
		if(tamZonaCalorObs > 1) {
			// Arriba
			if(centro.y-1 >= 0) {
				aplicarCalor(centro.x, centro.y-1, x, y-1, tamZonaCalorObs-1, fuerzaCalorObs-1, zonaObs);
			}
			// Derecha
			if(centro.x+1 < matrizNodos.size()) {
				aplicarCalor(centro.x+1, centro.y, x+1, y, tamZonaCalorObs-1, fuerzaCalorObs-1, zonaObs);
			}
			// Abajo
			if(centro.y+1 < matrizNodos.get(0).size()) {
				aplicarCalor(centro.x, centro.y+1, x, y+1, tamZonaCalorObs-1, fuerzaCalorObs-1, zonaObs);
			}
			// Izquierda
			if(centro.x-1 >= 0) {
				aplicarCalor(centro.x-1, centro.y, x-1, y, tamZonaCalorObs-1, fuerzaCalorObs-1, zonaObs);
			}
		}
		
		// Aplicar el calor obtenido en el mapa de calor
		for(int i = -(tamZonaCalorObs-1); i < tamZonaCalorObs; ++i) {
			for(int j = -(tamZonaCalorObs-Math.abs(i)-1); j < tamZonaCalorObs-Math.abs(i); ++j) {
				if(centro.x+i >= 0 && centro.x+i < mapaCalor.size() && centro.y+j >= 0 && centro.y+j < mapaCalor.get(0).size()) {
					mapaCalor.get(centro.x+i).get(centro.y+j).calorObstaculo += zonaObs[x+i][y+j];
				}
			}
		}
	}
	
	private void aplicarZonaCalorEne(Pos2D centro, int id) {
		// Crear zona de calor
		for(int i = 0; i < zonasEnemigas[id].length; ++i) {
			for(int j = 0; j < zonasEnemigas[id].length; ++j) {
				zonasEnemigas[id][i][j] = 0;
			}
		}
		
		// Fijar calor de alrededor del centro mediante recursividad
		int x = tamZonaCalorEne-1;
		int y = tamZonaCalorEne-1;
		aplicarCalor(centro.x, centro.y, x, y, tamZonaCalorEne, fuerzaCalorEne, zonasEnemigas[id]);
		
		// Aplicar el calor obtenido en el mapa de calor
		for(int i = -(tamZonaCalorEne-1); i < tamZonaCalorEne; ++i) {
			for(int j = -(tamZonaCalorEne-Math.abs(i)-1); j < tamZonaCalorEne-Math.abs(i); ++j) {
				if(centro.x+i >= 0 && centro.x+i < mapaCalor.size() && centro.y+j >= 0 && centro.y+j < mapaCalor.get(0).size()) {
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigos[id] += zonasEnemigas[id][x+i][y+j];
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigosTotal += zonasEnemigas[id][x+i][y+j];
				}
			}
		}
	}
	
	private void desaplicarZonaCalorEne(Pos2D centro, int id) {
		int x = tamZonaCalorEne-1;
		int y = tamZonaCalorEne-1;
		
		// Desaplicar el calor obtenido en el mapa de calor
		for(int i = -(tamZonaCalorEne-1); i < tamZonaCalorEne; ++i) {
			for(int j = -(tamZonaCalorEne-Math.abs(i)-1); j < tamZonaCalorEne-Math.abs(i); ++j) {
				if(centro.x+i >= 0 && centro.x+i < mapaCalor.size() && centro.y+j >= 0 && centro.y+j < mapaCalor.get(0).size()) {
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigos[id] -= zonasEnemigas[id][x+i][y+j];
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigosTotal -= zonasEnemigas[id][x+i][y+j];
				}
			}
		}
	}
	
	private void aplicarCalor(int pos_x, int pos_y, int x, int y, int tamZonaCalor, int fuerzaCalor, int[][] zona) {
		
		// Si el calor se transmite a un muro, el calor no pasa por él
		if(zona[x][y] < fuerzaCalor && !matrizNodos.get(pos_x).get(pos_y).obstaculo) {
			zona[x][y] = fuerzaCalor;
			
			if(tamZonaCalor > 1) {
				// Arriba
				aplicarCalor(pos_x, pos_y-1, x, y-1, tamZonaCalor-1, fuerzaCalor-1, zona);
				// Derecha
				aplicarCalor(pos_x+1, pos_y, x+1, y, tamZonaCalor-1, fuerzaCalor-1, zona);
				// Abajo
				aplicarCalor(pos_x, pos_y+1, x, y+1, tamZonaCalor-1, fuerzaCalor-1, zona);
				// Izquierda
				aplicarCalor(pos_x-1, pos_y, x-1, y, tamZonaCalor-1, fuerzaCalor-1, zona);
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
	
	private Object[] comprobarPerimetro() {
		
		int [] calorTotal = new int[4];
		int [] calorEnemigo = new int[4];
		
		for(int i = 0; i < 4; ++i) {
			switch (i) {
			case 0: // Arriba
				calorTotal[i] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).obtenerCalorTotal();
				
				calorEnemigo[i] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).calorEnemigosTotal;
				break;
			case 1: // Derecha
				calorTotal[i] = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).obtenerCalorTotal();
				
				calorEnemigo[i] = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).calorEnemigosTotal;
				break;
			case 2: // Abajo
				calorTotal[i] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).obtenerCalorTotal();
				
				calorEnemigo[i] = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).calorEnemigosTotal;
				break;
			case 3: // Izquierda
				calorTotal[i] = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).obtenerCalorTotal();
				
				calorEnemigo[i] = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).calorEnemigosTotal;
				break;
			}
			
			if(i != avatar.ori) {
				++calorTotal[i];
			}
		}
		
		// Si no hay peligro
		Boolean sinPeligro = true;
		
		for(int i = 0; i < 4 && sinPeligro; ++i) {
			if(calorEnemigo[i] != 0) {
				sinPeligro = false;
			}
		}
		
		if(gemasEncontradas && nivel == Nivel.REACTIVO_DELIB && !sinPeligro) {
			if(distanciaManhattan(avatar.xy, portal) == 1) {
				Boolean seguir = true;
				
				switch (avatar.ori) {
				case 0:
					if(avatar.xy.y <= portal.y) {
						seguir = false;
					}
					break;
				case 1:
					if(avatar.xy.x >= portal.x) {
						seguir = false;
					}				
					break;
				case 2:
					if(avatar.xy.y >= portal.y) {
						seguir = false;
					}
					break;
				case 3:
					if(avatar.xy.x <= portal.x) {
						seguir = false;
					}	
					break;
				}
				
				if(seguir) {
					sinPeligro = true;
					
					for(Pos2D enemigo : listaEnemigos) {
						if(enemigo.equals(portal)) {
							sinPeligro = false;
						}
					}
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
			if(calorTotal[i] < calorMin) {
				calorMin = calorTotal[i];
				elegidos.clear();
			}
			
			if(calorTotal[i] == calorMin) {
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
			int enemigoMasCercano = -1;
			int distancia = 3*matrizNodos.size();
			
			for(int i = 0; i < listaEnemigos.size(); ++i) {
				int distanciaAux = distanciaManhattan(listaEnemigos.get(i), avatar.xy);
				if(distanciaAux < distancia) {
					distancia = distanciaAux;
					enemigoMasCercano = i;
				}
				else if(distanciaAux == distancia) {
					enemigoMasCercano = -1;
 				}
			}
			
			if(enemigoMasCercano == -1) {
				int distObsMasCercano = 3*matrizNodos.size();
				int elegido = -1;
				
				for(int ori : elegidos) {
					Boolean terminar = false;
					int j = 2;
					int distanciaAux = 0;
					
					while(!terminar) {
						switch (ori) {
						case 0:
							if(matrizNodos.get(avatar.xy.x).get(avatar.xy.y-j).obstaculo) {
								terminar = true;
								distanciaAux = j;
							}
							break;
						case 1:
							if(matrizNodos.get(avatar.xy.x+j).get(avatar.xy.y).obstaculo) {
								terminar = true;
								distanciaAux = j;
							}
							break;
						case 2:
							if(matrizNodos.get(avatar.xy.x).get(avatar.xy.y+j).obstaculo) {
								terminar = true;
								distanciaAux = j;
							}		
							break;
						case 3:
							if(matrizNodos.get(avatar.xy.x-j).get(avatar.xy.y).obstaculo) {
								terminar = true;
								distanciaAux = j;
							}
							break;
						}
						
						++j;
					}
					
					if(distanciaAux > distObsMasCercano) {
						distObsMasCercano = distanciaAux;
						elegido = ori;
					}
				}
				
				salida[1] = elegido;
			}
			else {
				int elegido = -1;
				calorMin = 2*fuerzaCalorEne;
				
				for(int i : elegidos) {
					int calor = 0;
					switch (i) {
					case 0:
						calor = mapaCalor.get(avatar.xy.x).get(avatar.xy.y-1).calorEnemigos[enemigoMasCercano];
						break;
					case 1:
						calor = mapaCalor.get(avatar.xy.x+1).get(avatar.xy.y).calorEnemigos[enemigoMasCercano];
						break;
					case 2:
						calor = mapaCalor.get(avatar.xy.x).get(avatar.xy.y+1).calorEnemigos[enemigoMasCercano];				
						break;
					case 3:
						calor = mapaCalor.get(avatar.xy.x-1).get(avatar.xy.y).calorEnemigos[enemigoMasCercano];
						break;
					}
					
					if(calor < calorMin) {
						calorMin = calor;
						elegido = i;
					}
				}
				
				salida[1] = elegido;
			}
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
	}
	
	private void obtenerMejorPlanGema() {
		Pos2D elegido = null;
		int distancia = 4*matrizNodos.size();
		
		for(CasillaGema gema : listaGemas) {
			if(matrizNodos.get(gema.pos.x).get(gema.pos.y).gema &&
			   gema.casillaCalor.calorEnemigosTotal == 0) {
				int distanciaAux = distanciaManhattan(avatar.xy, gema.pos);
				
				if(distanciaAux < distancia) {
					distancia = distanciaAux;
					elegido = gema.pos;
				}
			}
		}
		
		if(elegido == null) {
			for(CasillaGema gema : listaGemas) {
				if(matrizNodos.get(gema.pos.x).get(gema.pos.y).gema) {
					int distanciaAux = distanciaManhattan(avatar.xy, gema.pos);
					
					if(distanciaAux < distancia) {
						distancia = distanciaAux;
						elegido = gema.pos;
					}
				}
			}
		}
		
		pathDisponibles.add(pathfinding_A_star(avatar, elegido));
		pathActivo = 0;
	}
	
	private Types.ACTIONS reactivo_deliberativo(StateObservation stateObs) {

		actualizarEnemigos(stateObs);
		
		// Comprobar situación alrededor del avatar
		Object[] resultado = comprobarPerimetro();
		
		Boolean sinPeligro = (Boolean)resultado[0];
		
		if(sinPeligro) {
			if(!irAObjetivo) {
				pathDisponibles.clear();
				if(!gemasEncontradas) {
					obtenerMejorPlanGema();
				}
				else {
					pathDisponibles.add(pathfinding_A_star(avatar, portal));
					pathActivo = 0;
				}
				
				irAObjetivo = true;
				
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
			else {
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
		}
		else {
			irAObjetivo = false;
			
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
	}
	
}
