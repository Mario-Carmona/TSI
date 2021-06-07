(define (domain ejercicio2)
    (:requirements :strips :typing :adl)
    (:types
        tipo_unidad tipo_edificio entidad localizacion recurso - object
        unidad edificio - entidad
    )
    (:constants
        VCE - tipo_unidad
        Centro_de_mando Barracones Extractor - tipo_edificio
        Mineral Gas_vespeno - recurso
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
        ; El edificio ?edi requiere tener el recurso ?recu para poder ser construido
        (edificioRequiere ?edi - edificio ?recu - recurso)
        ; El edificio ?edi está construido
        (construido ?edi - edificio)
        ; La localización ?loca está ocupada
        (ocupadaLoca ?loca - localizacion)
        ; Se dispone del recurso ?recu
        (disponibleRecu ?recu - recurso)
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
        :parameters (?uni - unidad ?loca - localizacion)
        :precondition 
            (and 
                ; La unidad está libre
                (libre ?uni)
                ; La unidad es un VCE
                (unidadEs ?uni VCE)
                ; La unidad ?uni se encuentra en la localización de extracción ?loca
                (unidadEn ?uni ?loca)
                (or
                    (depositoEn Mineral ?loca)
                    (and
                        (depositoEn Gas_vespeno ?loca)
                        (exists (?edi - edificio) 
                            (and
                                (edificioEs ?edi Extractor)
                                (edificioEn ?edi ?loca)
                            )
                        )
                    )
                )
            )
        :effect 
            (and 
                (when (depositoEn Gas_vespeno ?loca) 
                    (and
                        ; La unidad ?uni está extrayendo gas vespeno del nodo
                        (extrayendo ?uni Gas_vespeno)
                        ; Se dispone del recurso Gas Vespeno
                        (disponibleRecu Gas_vespeno)
                    )
                )
                (when (depositoEn Mineral ?loca) 
                    (and
                        ; La unidad ?uni está extrayendo mineral del nodo
                        (extrayendo ?uni Mineral)
                        ; Se dispone del recurso Mineral
                        (disponibleRecu Mineral)
                    )
                )
                ; La unidad ?uni está asignada en un trabajo en la localización ?loca
                (asignado ?uni ?loca)
                ; La unidad ?uni no está libre
                (not (libre ?uni))
            )
    )

    (:action construir
        :parameters (?uni - unidad ?edi - edificio ?loca - localizacion)
        :precondition 
            (and 
                ; El edificio ?edi no está construido, sólo se puede construir una vez un objeto edificio
                (not (construido ?edi))
                ; La localización ?loca no está ocupada
                (not (ocupadaLoca ?loca))
                ; La unidad está libre
                (libre ?uni)
                ; La unidad es un VCE
                (unidadEs ?uni VCE)
                ; La unidad ?uni se encuentra en la localización de construcción ?loca
                (unidadEn ?uni ?loca)
                (exists (?recu - recurso)
                    (or
                        (and
                            ; El edificio ?edi requiere el recurso ?recu para ser construido
                            (edificioRequiere ?edi ?recu)
                            ; Se dispone del recurso ?recu
                            (disponibleRecu ?recu)
                        )
                    )
                )
                (or
                    (and
                        (edificioEs ?edi Extractor)
                        (depositoEn Gas_vespeno ?loca)
                    )
                    (not (edificioEs ?edi Extractor))
                )
            )
        :effect 
            (and 
                ; El edificio ?edi está construido en la localización ?loca
                (edificioEn ?edi ?loca)
                ; El edificio ?edi está construido
                (construido ?edi)
                ; La localización ?loca está ocupada
                (ocupadaLoca ?loca)
            )
    )
    
)