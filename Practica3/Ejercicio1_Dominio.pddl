(define (domain ejercicio1)
    (:requirements :strips :typing :adl)
    (:types
        unidad edificio localizacion recurso - object
    )
    (:constants
        VCE - unidad
        Centro_de_mando barracones - edificio
        Mineral gas - recurso
    )
    (:predicates
        ; El edificio ?edi se encuentra en la localización ?x
        (edificioEn ?edi - edificio ?x - localizacion)
        ; La unidad ?uni se encuentra en la localización ?x
        (unidadEn ?uni - unidad ?x - localizacion)
        ; Existe un camino desde la localización ?x hasta la localización ?y
        (camino ?x - localizacion ?y - localizacion)
        ; Un depósito del recurso ?recu se encuentra en la localización ?x
        (depositoEn ?recu - recurso ?x - localizacion)
        ; La unidad ?vce está extrayendo el recurso ?recu
        (extrayendo ?vce - unidad ?recu - recurso)
    )

    ; Mover a una unidad entre dos localizaciones
    (:action navegar
        :parameters (?uni - unidad ?locaOri - localizacion ?locaDest - localizacion)
        :precondition 
            (and 
                ; La unidad se encuentra en la localización de origen
                (unidadEn ?uni ?locaOri)
                ; Existe un camino entre ambas localizaciones
                (camino ?locaOri ?locaDest)
            )
        :effect 
            (and 
                ; La unidad se encuentra en la localización de destino
                (unidadEn ?uni ?locaDest)
                ; La unidad no se encuentra en la localización de origen
                (not (unidadEn ?uni ?locaOri))
            )
    )

    ; Asignar un VCE a un nodo de recursos
    (:action asignar
        :parameters (?uni - unidad ?loca - localizacion ?recu - recurso)
        :precondition 
            (and 
                ; El nodo de recursos ?recu se encuentra en la localización de extracción ?loca
                (depositoEn ?recu ?loca)
                ; La unidad ?uni se encuentra en la localización de extracción ?loca
                (unidadEn ?uni ?loca)
            )
        :effect 
            (and 
                ; La unidad ?uni está extrayendo recursos del nodo de recursos ?recu
                (extrayendo ?uni ?recu)
            )
    )
)