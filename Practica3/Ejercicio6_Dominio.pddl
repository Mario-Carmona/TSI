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
        ; La unidad ?uni está libre
        (libre ?uni - unidad)
        ; El tipo de unidad ?tipoUni requiere tener el tipo de edificio ?tipoEdi para poder reclutarla
        (unidadRequiereEdi ?tipoUni - tipo_unidad ?tipoEdi - tipo_edificio)
        ; El edificio ?edi está construido
        (construido ?edi - edificio)
        ; La localización ?loca está ocupada
        (ocupadaLoca ?loca - localizacion)
        ; Se dispone del tipo de edificio ?tipoEdi en la localización ?loca
        (dispone ?tipoEdi - tipo_edificio ?loca - localizacion)
        ; Ha sido reclutada la unidad ?uni
        (reclutada ?uni - unidad)
    )
    (:functions
        ; Cantidad disponible del recurso ?recu
        (cantidad ?recu - recurso)
        ; Cantidad de recursos recolectados por VCE
        (cantidadPorVCE)
        ; Número de VCE's asignados en la localización ?loca
        (numeroVCE ?loca - localizacion)
        ; Cantidad del recurso ?recu requerido por el tipo de unidad ?tipoUni para ser reclutado
        (unidadRequiereRecu ?tipoUni - tipo_unidad ?recu - recurso)
        ; Cantidad del recurso ?recu requerido por el tipo de edificio ?tipoEdi para ser construido
        (edificioRequiereRecu ?tipoEdi - tipo_edificio ?recu - recurso)
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
                    ; Hay un deposito de Mineral en la localización ?loca
                    (depositoEn Mineral ?loca)
                    ; Se dispone de un Extractor en la localización ?loca, esto se lleva incluido
                    ; que haya un deposito de Gas vespeno en la localización
                    (dispone Extractor ?loca)
                )
            )
        :effect 
            (and 
                ; Incrementar en uno el número de VCE's asignados en la localización ?loca
                (increase (numeroVCE ?loca) 1)
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
                        ; Se dispone de más cantidad de minerales de los requeridos por el tipo de edificio ?tipoEdi
                        (>=
                            (cantidad Mineral)
                            (edificioRequiereRecu ?tipoEdi Mineral)
                        )
                        ; Se dispone de más cantidad de gas vespeno de los requeridos por el tipo de edificio ?tipoEdi
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
                ; Cuando el edificio ?edi es un Barracón
                (when (edificioEs ?edi Barracones) 
                    (and
                        ; Decrementar la cantidad de recursos de Mineral quitando la cantidad requerida por los Barracones
                        (decrease (cantidad Mineral) (edificioRequiereRecu Barracones Mineral))
                        ; Decrementar la cantidad de recursos de Gas vespeno quitando la cantidad requerida por los Barracones
                        (decrease (cantidad Gas_vespeno) (edificioRequiereRecu Barracones Gas_vespeno))
                        ; Se dispone de un Barracón en la localización ?loca
                        (dispone Barracones ?loca)
                    )
                )
                ; Cuando el edificio ?edi es un Extractor
                (when (edificioEs ?edi Extractor) 
                    (and
                        ; Decrementar la cantidad de recursos de Mineral quitando la cantidad requerida por el Extractor
                        (decrease (cantidad Mineral) (edificioRequiereRecu Extractor Mineral))
                        ; Decrementar la cantidad de recursos de Gas vespeno quitando la cantidad requerida por el Extractor
                        (decrease (cantidad Gas_vespeno) (edificioRequiereRecu Extractor Gas_vespeno))
                        ; Se dispone de un Extractor en la localización ?loca
                        (dispone Extractor ?loca)
                    )
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
                        ; Se dispone de más cantidad de minerales de los requeridos por el tipo de unidad ?tipoUni
                        (>=
                            (cantidad Mineral)
                            (unidadRequiereRecu ?tipoUni Mineral)
                        )
                        ; Se dispone de más cantidad de gas vespeno de los requeridos por el tipo de unidad ?tipoUni
                        (>=
                            (cantidad Gas_vespeno)
                            (unidadRequiereRecu ?tipoUni Gas_vespeno)
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
                ; Cuando la unidad ?uni es un VCE
                (when (unidadEs ?uni VCE) 
                    (and
                        ; Decrementar la cantidad de recursos de Mineral quitando la cantidad requerida por el VCE
                        (decrease (cantidad Mineral) (unidadRequiereRecu VCE Mineral))
                        ; Decrementar la cantidad de recursos de Gas vespeno quitando la cantidad requerida por el VCE
                        (decrease (cantidad Gas_vespeno) (unidadRequiereRecu VCE Gas_vespeno))
                    )
                )
                ; Cuando la unidad ?uni es un Marine
                (when (unidadEs ?uni Marine) 
                    (and
                        ; Decrementar la cantidad de recursos de Mineral quitando la cantidad requerida por el Marine
                        (decrease (cantidad Mineral) (unidadRequiereRecu Marine Mineral))
                        ; Decrementar la cantidad de recursos de Gas vespeno quitando la cantidad requerida por el Marine
                        (decrease (cantidad Gas_vespeno) (unidadRequiereRecu Marine Gas_vespeno))
                    )
                )
                ; Cuando la unidad ?uni es un Segador
                (when (unidadEs ?uni Segador) 
                    (and
                        ; Decrementar la cantidad de recursos de Mineral quitando la cantidad requerida por el Segador
                        (decrease (cantidad Mineral) (unidadRequiereRecu Segador Mineral))
                        ; Decrementar la cantidad de recursos de Gas vespeno quitando la cantidad requerida por el Segador
                        (decrease (cantidad Gas_vespeno) (unidadRequiereRecu Segador Gas_vespeno))
                    )
                )
            )
    )
    
    ; Recolectar los recursos de una localización
    (:action recolectar
        :parameters (?recu - recurso ?loca - localizacion)
        :precondition 
            (and 
                ; Hay un deposito del recurso ?recu en la localización ?loca
                (depositoEn ?recu ?loca)
                ; Hay al menos un VCE asignado a la localización ?loca
                (>
                    (numeroVCE ?loca)
                    0
                )
                ; Al recolectar no se excede el límite de 60 unidades del recurso ?recu
                (<=
                    (+
                        (cantidad ?recu)
                        (* (numeroVCE ?loca) (cantidadPorVCE))
                    )
                    60
                )
            )
        :effect 
            ; Incrementar la cantidad de recurso ?recu añadiendo la cantidad de recurso recolectados por
            ; los VCE's asignados en la localización ?loca
            (increase (cantidad ?recu) (* (numeroVCE ?loca) (cantidadPorVCE)))
    )
    
)