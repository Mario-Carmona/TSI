(define (domain ejercicio6)
    (:requirements :strips :typing :adl :fluents)
    (:types
        tipo_unidad tipo_edificio entidad localizacion recurso - object
        unidad edificio - entidad
    )
    (:constants
        VCE Marine Segador - tipo_unidad
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
        ; La unidad ?uni está libre
        (libre ?uni - unidad)
        ; El tipo de unidad ?tipoUni requiere tener el tipo de edificio ?tipoEdi para poder reclutarla
        (unidadRequiereEdi ?tipoUni - tipo_unidad ?tipoEdi - tipo_edificio)
        ; El edificio ?edi está construido
        (construido ?edi - edificio)
        ; La localización ?loca está ocupada
        (ocupadaLoca ?loca - localizacion)
        (reclutada ?uni - unidad)
        (dispone ?tipoEdi - tipo_edificio ?loca - localizacion)
    )
    (:functions
        (cantidad ?recu - recurso)
        (cantidadPorVCE)
        (numeroVCE ?loca - localizacion)
        (unidadRequiereRecu ?tipoUni - tipo_unidad ?recu - recurso)
        (edificioRequiereRecu ?tipoEdi - tipo_edificio ?recu - recurso)
    )

    ; Mover a una unidad entre dos localizaciones
    (:action navegar
        :parameters (?uni - unidad ?locaOri - localizacion ?locaDest - localizacion)
        :precondition 
            (and 
                (libre ?uni)
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
                    (dispone Extractor ?loca)
                )
            )
        :effect 
            (and 
                (increase (numeroVCE ?loca) 1)
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
                (or
                    (and
                        (edificioEs ?edi Extractor)
                        (depositoEn Gas_vespeno ?loca)
                    )
                    (not (edificioEs ?edi Extractor))
                )
                (exists (?tipoEdi - tipo_edificio) 
                    (and
                        ; El edificio ?edi es del tipo ?tipoEdi
                        (edificioEs ?edi ?tipoEdi)
                        (>=
                            (cantidad Mineral)
                            (edificioRequiereRecu ?tipoEdi Mineral)
                        )
                        (>=
                            (cantidad Gas_vespeno)
                            (edificioRequiereRecu ?tipoEdi Gas_vespeno)
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
                ; La localización ?loca está ocupada
                (ocupadaLoca ?loca)

                (when (edificioEs ?edi Barracones) 
                    (and
                        (decrease (cantidad Mineral) (edificioRequiereRecu Barracones Mineral))
                        (decrease (cantidad Gas_vespeno) (edificioRequiereRecu Barracones Gas_vespeno))
                        (dispone Barracones ?loca)
                    )
                )
                (when (edificioEs ?edi Extractor) 
                    (and
                        (decrease (cantidad Mineral) (edificioRequiereRecu Extractor Mineral))
                        (decrease (cantidad Gas_vespeno) (edificioRequiereRecu Extractor Gas_vespeno))
                        (dispone Extractor ?loca)
                    )
                )
            )
    )
    
    (:action reclutar
        :parameters (?edi - edificio ?uni - unidad ?loca - localizacion)
        :precondition 
            (and 
                (not (reclutada ?uni))
                (exists (?tipoUni - tipo_unidad ?tipoEdi - tipo_edificio)
                    (and
                        (unidadEs ?uni ?tipoUni)
                        (>=
                            (cantidad Mineral)
                            (unidadRequiereRecu ?tipoUni Mineral)
                        )
                        (>=
                            (cantidad Gas_vespeno)
                            (unidadRequiereRecu ?tipoUni Gas_vespeno)
                        )
                        (unidadEs ?uni ?tipoUni)
                        (edificioEs ?edi ?tipoEdi)
                        (unidadRequiereEdi ?tipoUni ?tipoEdi)
                        (edificioEn ?edi ?loca)
                    )
                )
            )
        :effect 
            (and 
                (libre ?uni)
                (reclutada ?uni)
                (unidadEn ?uni ?loca)

                (when (unidadEs ?uni VCE) 
                    (and
                        (decrease (cantidad Mineral) (unidadRequiereRecu VCE Mineral))
                        (decrease (cantidad Gas_vespeno) (unidadRequiereRecu VCE Gas_vespeno))
                    )
                )
                (when (unidadEs ?uni Marine) 
                    (and
                        (decrease (cantidad Mineral) (unidadRequiereRecu Marine Mineral))
                        (decrease (cantidad Gas_vespeno) (unidadRequiereRecu Marine Gas_vespeno))
                    )
                )
                (when (unidadEs ?uni Segador) 
                    (and
                        (decrease (cantidad Mineral) (unidadRequiereRecu Segador Mineral))
                        (decrease (cantidad Gas_vespeno) (unidadRequiereRecu Segador Gas_vespeno))
                    )
                )
            )
    )
    
    (:action recolectar
        :parameters (?recu - recurso ?loca - localizacion)
        :precondition 
            (and 
                (depositoEn ?recu ?loca)
                (>
                    (numeroVCE ?loca)
                    0
                )
                (<=
                    (+
                        (cantidad ?recu)
                        (* (numeroVCE ?loca) (cantidadPorVCE))
                    )
                    60
                )
            )
        :effect 
            (increase (cantidad ?recu) (* (numeroVCE ?loca) (cantidadPorVCE)))
    )
    
)