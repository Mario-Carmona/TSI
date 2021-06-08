(define (domain ejercicio4)
    (:requirements :strips :typing :adl)
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
        ; La unidad ?uni está libre
        (libre ?uni - unidad)
        ; El tipo de edificio ?tipoEdi requiere tener el recurso ?recu para poder ser construido
        (edificioRequiere ?tipoEdi - tipo_edificio ?recu - recurso)
        ; El tipo de unidad ?tipoUni requiere tener el recurso ?recu para poder reclutarla
        (unidadRequiereRecu ?tipoUni - tipo_unidad ?recu - recurso)
        ; El tipo de unidad ?tipoUni requiere tener el tipo de edificio ?tipoEdi para poder reclutarla
        (unidadRequiereEdi ?tipoUni - tipo_unidad ?tipoEdi - tipo_edificio)
        ; El edificio ?edi está construido
        (construido ?edi - edificio)
        ; La localización ?loca está ocupada
        (ocupadaLoca ?loca - localizacion)
        ; Se dispone del recurso ?recu
        (disponibleRecu ?recu - recurso)
        ; Se dispone del tipo de edificio ?tipoEdi en la localización ?loca
        (dispone ?tipoEdi - tipo_edificio ?loca - localizacion)
        ; Ha sido reclutada la unidad ?uni
        (reclutada ?uni - unidad)
    )

    ; Mover a una unidad entre dos localizaciones
    (:action navegar
        :parameters (?uni - unidad ?locaOri - localizacion ?locaDest - localizacion)
        :precondition 
            (and 
                ; La unidad ?uni está libre
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
                    ; Se dispone de un Extractor en la localización ?loca, esto se lleva incluido
                    ; que haya un deposito de Gas vespeno en la localización
                    (dispone Extractor ?loca)
                )
            )
        :effect 
            (and 
                ; Cuando hay un depósito de Gas vespeno en la localización ?loca
                (when (depositoEn Gas_vespeno ?loca) 
                    ; Se dispone del recurso Gas Vespeno
                    (disponibleRecu Gas_vespeno)
                )
                ; Cuando hay un depósito de Mineral en la localización ?loca
                (when (depositoEn Mineral ?loca) 
                    ; Se dispone del recurso Mineral
                    (disponibleRecu Mineral)
                )
                ; La unidad ?uni no está libre
                (not (libre ?uni))
            )
    )

    ; Construir un edificio con la ayuda de un VCE
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
                        ; El edificio ?edi es un Extractor
                        (edificioEs ?edi Extractor)
                        ; Hay un deposito de Gas vespeno en la localización ?loca
                        (depositoEn Gas_vespeno ?loca)
                    )
                    ; El edificio ?edi no es un Extractor
                    (not (edificioEs ?edi Extractor))
                )
                (exists (?tipoEdi - tipo_edificio) 
                    (and
                        ; El edificio ?edi es del tipo ?tipoEdi
                        (edificioEs ?edi ?tipoEdi)
                        (or
                            (and
                                ; El tipo de edificio ?tipoEdi requiere el recurso Mineral para ser construido
                                (edificioRequiere ?tipoEdi Mineral)
                                ; El tipo de edificio ?tipoEdi no requiere el recurso Gas vespeno para ser construido
                                (not (edificioRequiere ?tipoEdi Gas_vespeno))
                                ; Se dispone del recurso Mineral
                                (disponibleRecu Mineral)
                            )
                            (and
                                ; El tipo de edificio ?tipoEdi requiere el recurso Gas vespeno para ser construido
                                (edificioRequiere ?tipoEdi Gas_vespeno)
                                ; El tipo de edificio ?tipoEdi no requiere el recurso Mineral para ser construido
                                (not (edificioRequiere ?tipoEdi Mineral))
                                ; Se dispone del recurso Gas vespeno
                                (disponibleRecu Gas_vespeno)
                            )
                            (and
                                ; El tipo de edificio ?tipoEdi requiere el recurso Mineral para ser construido
                                (edificioRequiere ?tipoEdi Mineral)
                                ; El tipo de edificio ?tipoEdi requiere el recurso Gas vespeno para ser construido
                                (edificioRequiere ?tipoEdi Gas_vespeno)
                                ; Se dispone del recurso Mineral
                                (disponibleRecu Mineral)
                                ; Se dispone del recurso Gas vespeno
                                (disponibleRecu Gas_vespeno)
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
                ; La localización ?loca está ocupada
                (ocupadaLoca ?loca)
                ; Cuando el edificio ?edi es un Barracón
                (when (edificioEs ?edi Barracones) 
                    ; Se dispone de un Barracón en la localización ?loca
                    (dispone Barracones ?loca)
                )
                ; Cuando el edificio ?edi es un Extractor
                (when (edificioEs ?edi Extractor) 
                    ; Se dispone de un Extractor en la localización ?loca
                    (dispone Extractor ?loca)
                )
            )
    )
    
    ; Reclutar una unidad
    (:action reclutar
        :parameters (?edi - edificio ?uni - unidad ?loca - localizacion)
        :precondition 
            (and 
                ; La unidad ?uni todavía no ha sido reclutada
                (not (reclutada ?uni))
                (exists (?tipoUni - tipo_unidad ?tipoEdi - tipo_edificio)
                    (and
                        ; La unidad ?uni es de tipo ?tipoUni
                        (unidadEs ?uni ?tipoUni)
                        (or
                            (and
                                ; La unidad ?uni requiere el recurso Mineral para ser reclutado
                                (unidadRequiereRecu ?tipoUni Mineral)
                                ; La unidad ?uni no requiere el recurso Gas vespeno para ser reclutado
                                (not (unidadRequiereRecu ?tipoUni Gas_vespeno))
                                ; Se dispone del recurso Mineral
                                (disponibleRecu Mineral)
                            )
                            (and
                                ; La unidad ?uni requiere el recurso Gas Vespeno para ser reclutado
                                (unidadRequiereRecu ?tipoUni Gas_vespeno)
                                ; La unidad ?uni no requiere el recurso Mineral para ser reclutado
                                (not (unidadRequiereRecu ?tipoUni Mineral))
                                ; Se dispone del recurso Gas vespeno
                                (disponibleRecu Gas_vespeno)
                            )
                            (and
                                ; La unidad ?uni requiere el recurso Gas Vespeno para ser reclutado
                                (unidadRequiereRecu ?tipoUni Gas_vespeno)
                                ; La unidad ?uni requiere el recurso Mineral para ser reclutado
                                (unidadRequiereRecu ?tipoUni Mineral)
                                ; Se dispone del recurso Mineral
                                (disponibleRecu Mineral)
                                ; Se dispone del recurso Gas vespeno
                                (disponibleRecu Gas_vespeno)
                            )
                        )
                        ; El edificio ?edi es del tipo ?tipoEdi
                        (edificioEs ?edi ?tipoEdi)
                        ; Las unidades de tipo ?tipoUni requieren del tipo de edificio ?tipoEdi para ser reclutadas
                        (unidadRequiereEdi ?tipoUni ?tipoEdi)
                        ; El edificio ?edi se encuentra en la localización ?loca
                        (edificioEn ?edi ?loca)
                    )
                )
            )
        :effect 
            (and 
                ; La unidad ?uni está libre
                (libre ?uni)
                ; La unidad ?uni ya ha sido reclutada
                (reclutada ?uni)
                ; La unidad ?uni se encuentra en la localización ?loca
                (unidadEn ?uni ?loca)
            )
    )
    
)