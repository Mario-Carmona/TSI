(define (domain ejercicio3)
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
        ; El tipo de edificio ?tipoEdi requiere tener el recurso ?recu para poder ser construido
        (edificioRequiere ?tipoEdi - tipo_edificio ?recu - recurso)
        ; El edificio ?edi está construido
        (construido ?edi - edificio)
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
                    ; La unidad ?uni está extrayendo gas vespeno del nodo
                    (extrayendo ?uni Gas_vespeno)
                )
                (when (depositoEn Mineral ?loca) 
                    ; La unidad ?uni está extrayendo mineral del nodo
                    (extrayendo ?uni Mineral)
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
                ; No debe haber ningún edificio construido en la localización ?loca
                (forall (?ediAux - edificio) 
                    (not (edificioEn ?ediAux ?loca))
                )
                ; La unidad está libre
                (libre ?uni)
                ; La unidad es un VCE
                (unidadEs ?uni VCE)
                ; La unidad ?uni se encuentra en la localización de construcción ?loca
                (unidadEn ?uni ?loca)
                (exists (?otraUni1 - unidad ?otraUni2 - unidad ?tipoEdi - tipo_edificio)
                    (and
                        (edificioEs ?edi ?tipoEdi)
                        (or
                            (and
                                ; El edificio ?edi requiere el recurso Mineral para ser construido
                                (edificioRequiere ?tipoEdi Mineral)
                                (not (edificioRequiere ?tipoEdi Gas_vespeno))
                                ; Una de las otras unidades es un VCE
                                (unidadEs ?otraUni1 VCE)
                                ; Una de las unidades está extrayendo Mineral
                                (extrayendo ?otraUni1 Mineral)
                            )
                            (and
                                ; El edificio ?edi requiere el recurso Gas Vespeno para ser construido
                                (edificioRequiere ?tipoEdi Gas_vespeno)
                                (not (edificioRequiere ?tipoEdi Mineral))
                                ; Una de las otras unidades es un VCE
                                (unidadEs ?otraUni1 VCE)
                                ; Una de las unidades está extrayendo Gas Vespeno
                                (extrayendo ?otraUni1 Gas_vespeno)
                            )
                            (and
                                ; El edificio ?edi requiere el recurso Mineral y Gas Vespeno para ser construido
                                (edificioRequiere ?tipoEdi Mineral)
                                (edificioRequiere ?tipoEdi Gas_vespeno)
                                ; Ambas unidades son un VCE
                                (unidadEs ?otraUni1 VCE)
                                (unidadEs ?otraUni2 VCE)
                                ; Una de las unidades está extrayendo Mineral
                                (extrayendo ?otraUni1 Mineral)
                                ; La otra unidad está extrayendo Gas Vespeno
                                (extrayendo ?otraUni2 Gas_vespeno)
                            )
                        )
                    )
                )
            )
        :effect 
            (and 
                ; El edificio ?edi está construido en la localización ?loca
                (edificioEn ?edi ?loca)
                ; El edificio ?edi está construido
                (construido ?edi)
            )
    )
    
)