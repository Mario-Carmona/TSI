(define (domain ejercicio1)
    (:requirements :strips :typing :adl)
    (:types
        tipo_unidad tipo_edificio entidad localizacion recurso - object
        unidad edificio - entidad
    )
    (:constants
        VCE - tipo_unidad
        Centro_de_mando Barracones - tipo_edificio
        Mineral Gas - recurso
    )
    (:predicates
        ; El edificio ?edi es un tipo de edificio ?tipo
        (edificioEs ?edi - edificio ?tipo - tipo_edificio)
        ; El edificio ?edi se encuentra en la localización ?loca
        (edificioEn ?edi - edificio ?loca - localizacion)
        ; La unidad ?uni es un tipo de unidad ? tipo
        (unidadEs ?uni - unidad ?tipo - tipo_unidad)
        ; La unidad ?uni se encuentra en la localización ?loca
        (unidadEn ?uni - unidad ?loca - localizacion)
        ; Existe un camino desde la localización ?locaOri hasta la localización ?locaDest
        (camino ?locaOri - localizacion ?locaDest - localizacion)
        ; Un depósito del recurso ?recu se encuentra en la localización ?loca
        (depositoEn ?recu - recurso ?loca - localizacion)
        ; La unidad ?uni está asignada en la localización ?loca
        (asignado ?uni - unidad ?loca - localizacion)
        ; La unidad ?uni está extrayendo el recurso ?recu
        (extrayendo ?uni - unidad ?recu - recurso)
        ; La unidad ?uni está libre
        (libre ?uni - unidad)
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
                ; La unidad está libre
                (libre ?uni)
                ; La unidad es un VCE
                (unidadEs ?uni VCE)
                ; El nodo de recursos ?recu se encuentra en la localización de extracción ?loca
                (depositoEn ?recu ?loca)
                ; La unidad ?uni se encuentra en la localización de extracción ?loca
                (unidadEn ?uni ?loca)
            )
        :effect 
            (and 
                ; La unidad ?uni está extrayendo recursos del nodo de recursos ?recu
                (extrayendo ?uni ?recu)
                ; La unidad ?uni está asignada en un trabajo en la localización ?loca
                (asignado ?uni ?loca)
                ; La unidad ?uni no está libre
                (not (libre ?uni))
            )
    )
)