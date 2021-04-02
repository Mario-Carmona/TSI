package TSI;

import ontology.Types;

public class nodo {
	Types.ACTIONS plan;
	int coste;
	nodo padre;
	
	public nodo() {
		plan = Types.ACTIONS.ACTION_NIL;
		coste = 0;
		padre = null;
	}
}
