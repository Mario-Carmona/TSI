package src_Carmona_Segovia_Mario;

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


public class Agent extends AbstractPlayer {

	// Representación del mapa
	
	/*
	 * Vector2d que nos permite el paso de píxeles a grid
	 */
	Vector2d fescala;
	/*
	 * Matriz que representa el mapa como un grid, cada
	 * elemento es un objeto Casilla
	 */
	ArrayList<ArrayList<Casilla>> matrizNodos;
	
	// Planes
	
	/*
	 * Lista de los planes que el avatar tiene que realizar
	 */
	ArrayList<Path> pathDisponibles;
	/*
	 * Variable que indica el índice del plan que se
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
	
	/*
	 * Matriz con el calor de cada casilla
	 */
	ArrayList<ArrayList<CasillaCalor>> mapaCalor;
	/*
	 * Lista con el rango de las distintas casillas del mapa
	 */
	SortedSet<CasillaRango> listaRangoCasillas;
	
		// Fuerza del calor y su dispersión
	
	/*
	 * Fuerza: es el valor del calor en el centro del objeto
	 * que produce el calor
	 * 
	 * Dispersión: es la cantidad de saltos entre casilla que se
	 * pueden hacer antes de que el calor sea cero. En cada salto
	 * el calor pierde una unidad de fuerza.
	 */
	
	/*
	 * Fuerza y dispersión del calor en los enemigos
	 */
	int tamZonaCalorEne;
	int fuerzaCalorEne;
	/*
	 * Fuerza y dispersión del calor en los obstáculos
	 */
	int tamZonaCalorObs;
	int fuerzaCalorObs;
		
		// Zonas
	
	/*
	 * Zona de calor de los enemigos
	 */
	int [][][] zonasEnemigas;
	/*
	 * Zona de calor de los obstaculos
	 */
	int [][] zonaObs;
	
		// Variables de control
	
	/*
	 * Indica si se está siguiendo un camino para ir
	 * a un lugar seguro
	 */
	Boolean irALugarSeguro;
	/*
	 * Indica si se está siguiendo un camino para ir a un objetivo
	 */
	Boolean irAObjetivo;
	
	// Varibles de control generales
	
	/*
	 * Indica el nivel del mapa
	 */
	Nivel nivel;
	/*
	 * Indica si han encontrado todas la gemas necesarias
	 */
	Boolean gemasEncontradas;
	/*
	 * Indica el número de gemas encontradas
	 */
	int numGemasEncontradas;
	/*
	 * Indica el número de gemas necesarias para ganar
	 */
	int numGemasNecesarias;
	
	
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

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
			
			// Calcular la distancia manhattan entre los objetivos(gemas)
			calcularDistanciaEntreObjetivos();
			
			// Obtener la ruta de objetivos
			ArrayList<Pos2D> listaPosiciones = greedy_manhattan();
			
			// Obtener los caminos para realizar la ruta de objetivos
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
			
			// Asignar fuerza y dispersión del calor
			// a los enemigos y obstáculos
			tamZonaCalorEne = 5;
			fuerzaCalorEne = 5;
			tamZonaCalorObs = 1;
			fuerzaCalorObs = 10;
			
			// Crear las zonas de calor. Esto nos será útil para los enemigos,
			// ya que en esta zona guardaremos la zona de calor de un enemigo
			// y podremos usarla para desaplicar el calor de un enemigo al 
			// moverse de casilla
			zonasEnemigas = new int[listaEnemigos.size()][2*tamZonaCalorEne - 1][2*tamZonaCalorEne - 1];
			zonaObs = new int[2*tamZonaCalorObs - 1][2*tamZonaCalorObs - 1];
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
			
	        // Inicializar el mapa de calor
			inicializarMapaCalor();
			
			// Inicializar la lista de rangos
			inicializarListaRangoCasilla(stateObs);
			break;
		case REACTIVO_DELIB:
			gemasEncontradas = false;
			numGemasEncontradas = 0;
			numGemasNecesarias = 9;
			
			irAObjetivo = false;
			
			// Asignar fuerza y dispersión del calor
						// a los enemigos y obstáculos
			tamZonaCalorEne = 5;
			fuerzaCalorEne = 5;
			tamZonaCalorObs = 1;
			fuerzaCalorObs = 10;
			
			// Crear las zonas de calor. Esto nos será útil para los enemigos,
			// ya que en esta zona guardaremos la zona de calor de un enemigo
			// y podremos usarla para desaplicar el calor de un enemigo al 
			// moverse de casilla
			zonasEnemigas = new int[listaEnemigos.size()][2*tamZonaCalorEne - 1][2*tamZonaCalorEne - 1];
			zonaObs = new int[2*tamZonaCalorObs - 1][2*tamZonaCalorObs - 1];
			
			// Obtener posición del avatar
	        obtenerAvatar(stateObs);
	        
	        // Obtener posición de las gemas y marcarlas en el mapa
			marcarGemas(stateObs);
			
			// Obtener coordenadas del portal
			obtenerPortal(stateObs);
			
			// Inicializar el mapa de calor
			inicializarMapaCalor();
			break;
		}
	}
	
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		
		Types.ACTIONS accion = Types.ACTIONS.ACTION_NIL;
		
		// Actualizamos la posición del avatar
		obtenerAvatar(stateObs);
		
		// Comprobar si hemos cogido una gema
		if(matrizNodos.get(avatar.xy.x).get(avatar.xy.y).gema) {
			// Eliminamos la gema del tablero, para evitar que sea contada
			// más veces al pasar por ella
			matrizNodos.get(avatar.xy.x).get(avatar.xy.y).gema = false;
			
			// Si se estaba yendo a un objetivo se indica que ya se ha
			// llegado
			irAObjetivo = false;
			
			// Aumentamos en uno las cantidad de gemas recogidas
			++numGemasEncontradas;
		}
		
		// Comprobar si ya he cogido todas las gemas necesarias
		if(numGemasEncontradas == numGemasNecesarias) {
			gemasEncontradas = true;
		}
		
		// Realización de la estrategia adecuada para cada nivel
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
	
	/*
	 * Método para obtener la posiciones de los enemigos
	 */
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
	
	/*
	 * Método para crear el tablero de nodos y el mapa de calor
	 */
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
	
	/*
	 * Método para obtener las posiciones de los obstáculos
	 */
	private void obtenerObstaculos(StateObservation stateObs) {
		listaObstaculos = new ArrayList<Pos2D>();
		for(ArrayList<Observation> resource: stateObs.getImmovablePositions()) {
			for(Observation obstaculo : resource) {
				Pos2D pos = convertirPixelAGrid(obstaculo);
	            listaObstaculos.add(pos);
			}
        }
	}
	
	/*
	 * Método para marcar los obstaculos en la matriz de nodos
	 */
	private void marcarObstaculos() {
		for(Pos2D obstaculo : listaObstaculos) {
            matrizNodos.get(obstaculo.x).get(obstaculo.y).setObstaculo(true);
		}
	}
	
	/*
	 * Método para obtener las posiciones de las gemas, marcarlas en
	 * la matriz de nodos y añadirlas a la lista de objetivos
	 */
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
	
	/*
	 * Método para obtener la posición del avatar
	 */
	private void obtenerAvatar(StateObservation stateObs) {
		avatar = new Pos3D((int)Math.floor(stateObs.getAvatarPosition().x / fescala.x), 
						   (int)Math.floor(stateObs.getAvatarPosition().y / fescala.y),
						   obtenerOriAvatar(new Pos2D(stateObs.getAvatarOrientation())));
	}
	
	/*
	 * Método para obtener la orientación del avatar
	 */
	private int obtenerOriAvatar(Pos2D pos) {
		if(pos.y == -1) { return 0; }	// Arriba
		if(pos.x == 1) { return 1; }	// Derecha
		if(pos.y == 1) { return 2; }	// Abajo
		if(pos.x == -1) { return 3; }	// Izquierda
		
		return -1;
	}
	
	/*
	 * Método para obtener la posición del portal
	 */
	private void obtenerPortal(StateObservation stateObs) {
		portal = convertirPixelAGrid(stateObs.getPortalsPositions()[0].get(0));
	}	
	
	/*
	 * Método para obtener la distancia manhattan entre dos puntos
	 */
	private int distanciaManhattan(Pos2D current, Pos2D destino) {
		return Math.abs(current.x - destino.x) + Math.abs(current.y - destino.y);
	}
	
	/*
	 * Método para convertir una posición en píxeles en una posición del grid
	 */
	private Pos2D convertirPixelAGrid(Observation observacion) {
		return new Pos2D((int)Math.floor(observacion.position.x / fescala.x), 
						 (int)Math.floor(observacion.position.y / fescala.y));
	}
	
	/****************************************************************************************/
	/****  Deliberativo                                                                     */
	/****************************************************************************************/
	
	/*
	 * Método para generar un camino entre una posición origen y una posición destino mediante
	 * el algoritmo A*
	 */
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
			
			// Obtener lista de acciones para recorrer
			// el camino
			Nodo iter = current;
			while(iter.plan.get(0) != ACTIONS.ACTION_NIL) {
				// Este bucle for se hace porque según la orientación
				// se debe realizar dos acciones para ir a ciertos nodos
				
				for(ACTIONS accion : iter.plan) {
					salida.addAccionInicio(accion);
				}
				iter = iter.padre;
			}
		}
		
		return salida;
	}
	
	/*
	 * Método para calcular la distancia manhattan entre los distintos objetivos del mapa
	 */
	private void calcularDistanciaEntreObjetivos() {
		distanciaGreedy = new int[listaObjetivos.size()][listaObjetivos.size()];
		
		for(NodoGreedy objetivo1 : listaObjetivos) {
			for(NodoGreedy objetivo2 : listaObjetivos) {
				distanciaGreedy[objetivo1.id][objetivo2.id] = distanciaManhattan(objetivo1.pos, objetivo2.pos);
			}
		}
	}
	
	/*
	 * Método para generar una ruta de objetivos medienta el algoritmo greedy
	 */
	private ArrayList<Pos2D> greedy_manhattan() {
		int gemas = 0;
		
		int valor_max = 3*matrizNodos.size();
		
		int min = valor_max;
		NodoGreedy nodoMin = null;
		int nodoElegido;
		
		Set<NodoGreedy> lista = new HashSet<NodoGreedy>(listaObjetivos);
		
		// Elegir la gema más cercana al avatar
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
		
		// Elegir el resto de las gemas, eligiendo la más cercana a la anterior elegida
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
		
		// Añadir el portal a la ruta
		salida.add(portal);
		
		return salida;
	}
	
	private Types.ACTIONS deliberativo_simple() {
		// Se devuelve la acción siguiente del camino para ir al portal
		Types.ACTIONS accion = pathDisponibles.get(0).siguienteAccion();
		return accion;
	}
	
	private Types.ACTIONS deliberativo_compuesto() {
		
		// Se obtiene la siguiente acción en el camino
		ACTIONS accion = pathDisponibles.get(pathActivo).siguienteAccion();
		
		// Si la acción indica que se ha llegado al final de camino
		if(accion == ACTIONS.ACTION_NIL) {
			// Se cambia al siguiente camino en la ruta de objetivos
			pathActivo = pathActivo + 1;
			
			// Se obtiene la primera acción del plan
			accion = pathDisponibles.get(pathActivo).siguienteAccion();
		}
		
		if(gemasEncontradas) {
			if(pathDisponibles.get(pathActivo).posDestino.xy != portal) {
				// Si ya tengo todas las gemas y el camino actual no me lleva
				// al portal, calculo un nuevo camino que me lleve al portal
				pathDisponibles.clear();
				
				pathDisponibles.add(pathfinding_A_star(avatar, portal));
				pathActivo = 0;
				
				// Obtengo la primera acción del camino
				accion = pathDisponibles.get(pathActivo).siguienteAccion();
			}
		}
		
		return accion;
	}
	
	
	/****************************************************************************************/
	/****  Reactivo                                                                         */
	/****************************************************************************************/
	
	/*
	 * Método para inicializar el mapa de calor
	 */
	private void inicializarMapaCalor() {
		// Aplicamos el calor de los obstáculos
		for(Pos2D obstaculo : listaObstaculos) {
			aplicarZonaCalorObs(obstaculo);
		}
		
		// Aplicamos el calor de los enemigos
		for(int i = 0; i < listaEnemigos.size(); ++i) {
			aplicarZonaCalorEne(listaEnemigos.get(i), i);
		}
	}
	
	/*
	 * Método para inicializar la lista de rangos de las distintas casillas
	 */
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
	
	/*
	 * Método para obtener el rango de una cierta posición
	 */
	private int obtenerObstaculoMasCercano(Pos2D posicion) {
		int rangoMin = mapaCalor.size()+1;
		
		for(Pos2D obstaculo : listaObstaculos) {
			// Obtengo el rango de un obstáculo
			int nuevoRango = obtenerRango(posicion, obstaculo);
			// Me quedo con el menor rango obtenido
			if(nuevoRango < rangoMin) {
				rangoMin = nuevoRango;
			}
		}
		
		return rangoMin;
	}
	
	/*
	 * Método para obtener el rango de una posición hacia un obstáculo
	 */
	private int obtenerRango(Pos2D posicion, Pos2D obstaculo) {
		int diferenciaX = Math.abs(posicion.x - obstaculo.x);
		int diferenciaY = Math.abs(posicion.y - obstaculo.y);
		
		// Devuelvo la diferencia más grande
		return Math.max(diferenciaX, diferenciaY);
	}
	
	/*
	 * Método para aplicar la zona de calor de un obstáculo
	 */
	private void aplicarZonaCalorObs(Pos2D centro) {
		// Inicializar la zona poniendo todas las casillas a 0
		for(int i = 0; i < zonaObs.length; ++i) {
			for(int j = 0; j < zonaObs.length; ++j) {
				zonaObs[i][j] = 0;
			}
		}
		
		// Fijar calor del centro de la zona
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
		
		// Aplicar el calor obtenido sumandolo al mapa de calor en las posiciones correspondientes
		for(int i = -(tamZonaCalorObs-1); i < tamZonaCalorObs; ++i) {
			for(int j = -(tamZonaCalorObs-Math.abs(i)-1); j < tamZonaCalorObs-Math.abs(i); ++j) {
				if(centro.x+i >= 0 && centro.x+i < mapaCalor.size() && centro.y+j >= 0 && centro.y+j < mapaCalor.get(0).size()) {
					mapaCalor.get(centro.x+i).get(centro.y+j).calorObstaculo += zonaObs[x+i][y+j];
				}
			}
		}
	}
	
	/*
	 * Método para aplicar la zona de calor del enemigo con cierto identificador id
	 * 
	 * Ejemplo de zona de calor con fuerza igual a 5 y dispersión igual a 5, el
	 * centro es donde está el cinco:
	 * 
	 *          1
	 *        1 2 1
	 *      1 2 3 2 1
	 *    1 2 3 4 3 2 1
	 *  1 2 3 4 5 4 3 2 1
	 *    1 2 3 4 3 2 1
	 *      1 2 3 2 1
	 *        1 2 1
	 *          1
	 *          
	 * Ejemplo de zona de calor con fuerza igual a 5 y dispersión igual a 3, el
	 * centro es donde está el cinco:
	 * 
	 *      3
	 *    3 4 3
	 *  3 4 5 4 3
	 *    3 4 3
	 *      3
	 * 
	 */
	private void aplicarZonaCalorEne(Pos2D centro, int id) {
		// Inicializar la zona poniendo todas las casillas a 0
		for(int i = 0; i < zonasEnemigas[id].length; ++i) {
			for(int j = 0; j < zonasEnemigas[id].length; ++j) {
				zonasEnemigas[id][i][j] = 0;
			}
		}
		
		// Fijar calor del centro de la zona mediante recursividad
		int x = tamZonaCalorEne-1;
		int y = tamZonaCalorEne-1;
		aplicarCalor(centro.x, centro.y, x, y, tamZonaCalorEne, fuerzaCalorEne, zonasEnemigas[id]);
		
		// Aplicar el calor obtenido sumandolo al mapa de calor en las posiciones correspondientes
		for(int i = -(tamZonaCalorEne-1); i < tamZonaCalorEne; ++i) {
			for(int j = -(tamZonaCalorEne-Math.abs(i)-1); j < tamZonaCalorEne-Math.abs(i); ++j) {
				if(centro.x+i >= 0 && centro.x+i < mapaCalor.size() && centro.y+j >= 0 && centro.y+j < mapaCalor.get(0).size()) {
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigos[id] += zonasEnemigas[id][x+i][y+j];
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigosTotal += zonasEnemigas[id][x+i][y+j];
				}
			}
		}
	}
	
	/*
	 * Método para desaplicar la zona de calor del enemigo con cierto identificador id.
	 * 
	 * Para este método se usa la zona de ese enemigo que ha sido calculada en la acción
	 * anterior, por lo que no hay que volver a calcularla.
	 */
	private void desaplicarZonaCalorEne(Pos2D centro, int id) {
		// Se calcula la posición del centro de la zona
		int x = tamZonaCalorEne-1;
		int y = tamZonaCalorEne-1;
		
		// Desaplicar el calor obtenido restandolo al mapa de calor en las posiciones correspondientes
		for(int i = -(tamZonaCalorEne-1); i < tamZonaCalorEne; ++i) {
			for(int j = -(tamZonaCalorEne-Math.abs(i)-1); j < tamZonaCalorEne-Math.abs(i); ++j) {
				if(centro.x+i >= 0 && centro.x+i < mapaCalor.size() && centro.y+j >= 0 && centro.y+j < mapaCalor.get(0).size()) {
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigos[id] -= zonasEnemigas[id][x+i][y+j];
					mapaCalor.get(centro.x+i).get(centro.y+j).calorEnemigosTotal -= zonasEnemigas[id][x+i][y+j];
				}
			}
		}
	}
	
	/*
	 * Método para calcular el calor de un objeto
	 */
	private void aplicarCalor(int pos_x, int pos_y, int x, int y, int tamZonaCalor, int fuerzaCalor, int[][] zona) {
		
		// Si la posición del centro es un muro, no se asigna calor a esa zona.
		//
		// Y si el calor en esa zona ya es mayor que el calor que se quiere asignar
		// no se aplica el calor
		if(zona[x][y] < fuerzaCalor && !matrizNodos.get(pos_x).get(pos_y).obstaculo) {
			// Se asigna calor a la posición central
			zona[x][y] = fuerzaCalor;
			
			// Si quedan al menos dos movimientos de casilla se lanzan los descendientes de esta posición
			// decrementando en una unidad la fuerza y dispersión del calor
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
	
	/*
	 * Método para actualizar las posiciones de los enemigos, así como
	 * el calor que producen
	 */
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
	
	/*
	 * Método para comprobar si hay peligro alrededor del avatar y
	 * si hay peligro obtener el mejor movimiento para esquivarlo
	 */
	private Object[] comprobarPerimetro() {
		
		int [] calorTotal = new int[4];
		int [] calorEnemigo = new int[4];
		
		// Obtener el calor de los enemigos y los obstáculos de cada orientación
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
			
			// Si la orientación es distinta a la orientación del avatar se incrementa el calor total
			if(i != avatar.ori) {
				++calorTotal[i];
			}
		}
		
		Boolean sinPeligro = true;
		
		// Comprobamos si alrededor, todas las posiciones tienen calor igual a cero
		for(int i = 0; i < 4 && sinPeligro; ++i) {
			if(calorEnemigo[i] != 0) {
				sinPeligro = false;
			}
		}
		
		// Si a pesar de obtener que hay riesgo, si nos encontramos en el nivel
		// reactivo deliberativo, hemos encontrado todas las gemas y tenemos el portal
		// al lado sin enemigos encima, nos dirigimos a él, por lo que se indica que no hay
		// peligro
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
		
		// Si no hay peligro se indica en la salida
		if(sinPeligro) {
			Object[] salida = new Object[1];
			salida[0] = sinPeligro; // No hay peligro cercano
			return salida;
		}
		
		
		// Se eligen las posiciones de cada orientación con menor calor
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
		
		// Actuación según el número de orientaciones elegidas
		Object[] salida = new Object[2];
		salida[0] = sinPeligro; // Hay peligro cercano
		switch (elegidos.size()) {
		case 1: // Huye en la única dirección elegida
			salida[1] = elegidos.get(0);
			break;
		case 2: // Huye a la dirección con menor calor del enemigo más cercano
		case 3:
		case 4:
			// Calculamos cuál es el enemigo más cercano al avatar
			int enemigoMasCercano = -1;
			int distancia = 3*matrizNodos.size();
			
			for(int i = 0; i < listaEnemigos.size(); ++i) {
				int distanciaAux = distanciaManhattan(listaEnemigos.get(i), avatar.xy);
				if(distanciaAux < distancia) {
					distancia = distanciaAux;
					enemigoMasCercano = i;
				}
				else if(distanciaAux == distancia) {
					// Si hay dos a la misma distancia se debe tener en cuenta
					enemigoMasCercano = -1;
 				}
			}
			
			// Si hay dos enemigos a la misma distancia
			if(enemigoMasCercano == -1) {
				// Obtenemos la distancia que hay entre el avatar y el obstáculo
				// más cercano en esa dirección, y nos quedamos con la dirección
				// que nos proporcione una mayor distancia, priorizando tener
				// una mayor capacidad de huida
				int distObsMasCercano = 3*matrizNodos.size();
				int elegido = -1;
				
				for(int ori : elegidos) {
					Boolean terminar = false;
					int j = 2;
					int distanciaAux = 0;
					
					// Avance por la dirección de la iteración
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
			else { // Si hay un enemigo que está más cerca
				
				// Se calcula cuál es la dirección de entre las elegidas que tiene un menor calor de ese 
				// enemigo, por lo que tratamos de alejarnos de él, al ser nuestro principal peligro.
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
	
	/*
	 * Método para obtener la casilla con mayor rango que no tiene presencia
	 * de calor de los enemigos y que está más cerca del avatar
	 */
	private Pos2D seleccionarMejorPos() {
		Iterator<CasillaRango> iter = listaRangoCasillas.iterator();
		int rango = listaRangoCasillas.first().rango;
		Pos2D elegido = listaRangoCasillas.first().pos;
		int distancia = distanciaManhattan(avatar.xy, elegido);
		Boolean parar = false;
		
		iter.next();
		// Se busca hasta que se encuentra un rango menor del que se tiene en rango,
		// ya que están ordenados de mayor a menor rango. Se busca la casilla más 
		// cercana al avatar
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
		// Actualizo la posición y el calor de los enemigos
		actualizarEnemigos(stateObs);
		
		// Compruebo si hay peligro alrededor del avatar, y si hay peligro
		// nos devuelve la mejor posición para evitarlo
		Object[] resultado = comprobarPerimetro();
		
		Boolean sinPeligro = (Boolean)resultado[0];
		
		// Si no hay peligro
		if(sinPeligro) {
			// Si no estamos siguiendo un camino que nos lleve a
			// una casilla segura
			if(!irALugarSeguro) {
				// Obtengo la mejor casilla, que será la casilla con mayor
				// rango que no tenga presencia de calor de enemigos
				Pos2D mejorPos = seleccionarMejorPos();
				
				// Obtenemos el camino con destino a la casilla segura
				pathDisponibles.clear();
				pathDisponibles.add(pathfinding_A_star(avatar, mejorPos));
				pathActivo = 0;
				
				// Se indica que se está siguiendo el camino
				irALugarSeguro = true;
				
				// Obtenemos la primera acción del camino
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
			else {
				// Obtenemos la siguiente acción del camino. Si ya hemos llegado al objetivo
				// no hay problema, ya que este método nos devuelve la acción nil cuando ya 
				// hemos terminado el camino, lo que permite que el avatar se quede quieto 
				// en la casilla segura
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
		}
		else {
			// Se indica que se deja de ir a una casilla segura porque
			// se ha detectado peligro
			irALugarSeguro = false;
			
			// Realizamos la acción para evitar el peligro
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
	
	/*
	 * Método para obtener la gema más cercana al avatar
	 */
	private void obtenerMejorPlanGema() {
		Pos2D elegido = null;
		int distancia = 4*matrizNodos.size();
		
		// Primero se busca la gemas más cercana que no tenga calor de
		// los enemigos
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
		
		// Si no se encuentra una gemas sin calor de los enemigos, se
		// quita esta restricción
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
		// Actualizo la posición y el calor de los enemigos
		actualizarEnemigos(stateObs);
		
		// Compruebo si hay peligro alrededor del avatar, y si hay peligro
		// nos devuelve la mejor posición para evitarlo
		Object[] resultado = comprobarPerimetro();
		
		Boolean sinPeligro = (Boolean)resultado[0];
		
		// Si no hay peligro
		if(sinPeligro) {
			// Si no estamos siguiendo un camino que nos lleve a
			// un objetivo
			if(!irAObjetivo) {
				// Eliminamos los caminos guardados
				pathDisponibles.clear();
				
				// Si no he recogido las gemas necesarias
				if(!gemasEncontradas) {
					// Obtengo el mejor camino para ir a una gema
					obtenerMejorPlanGema();
				}
				else {
					// Obtengo el camino para ir al portal
					pathDisponibles.add(pathfinding_A_star(avatar, portal));
					pathActivo = 0;
				}
				
				// Se indica que se está siguiendo el camino
				irAObjetivo = true;
				
				// Obtenemos la primera acción del camino
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
			else {
				// Obtenemos la siguiente acción del camino. Si ya hemos llegado al objetivo
				// no hay problema, ya que al coger la gema al inicio del método "act" indicamos
				// que dejamos de seguir el camino al objetivo.
				return pathDisponibles.get(pathActivo).siguienteAccion();
			}
		}
		else {
			// Se indica que se deja de ir al objetivo porque
			// se ha detectado peligro
			irAObjetivo = false;
			
			// Realizamos la acción para evitar el peligro
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
