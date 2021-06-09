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
        ; La unidad ?uni está libre
        (libre ?uni - unidad)
        ; El edificio ?edi requiere tener el recurso ?recu para poder ser construido
        (edificioRequiere ?tipoEdi - tipo_edificio ?recu - recurso)
        ; El edificio ?edi está construido
        (construido ?edi - edificio)
        ; La localización ?loca está ocupada
        (ocupadaLoca ?loca - localizacion)
        ; Se dispone del recurso ?recu
        (disponibleRecu ?recu - recurso)
        ; Se dispone del tipo de edificio ?tipoEdi en la localización ?loca
        (dispone ?tipoEdi - tipo_edificio ?loca - localizacion)
    )

    ; Mover a una unidad entre dos localizaciones
    (:action navegar
        :parameters (?uni - unidad ?locaOri - localizacion ?locaDest - localizacion)
        :precondition 
            (and 
                ; La unidad ?uni está libre
                (libre ?uni)
                ; La unidad ?uni se encuentra en la localización de origen ?locaOri
                (unidadEn ?uni ?locaOri)
                ; Existe un camino entre ambas localizaciones
                (camino ?locaOri ?locaDest)
            )
        :effect 
            (and 
                ; La unidad ?uni se encuentra en la localización de destino ?locaDest
                (unidadEn ?uni ?locaDest)
                ; La unidad ?uni no se encuentra en la localización de origen ?locaOri
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
                (exists (?recu - recurso ?tipoEdi - tipo_edificio)
                    (and
                        ; El edificio ?edi es del tipo ?tipoEdi
                        (edificioEs ?edi ?tipoEdi)
                        ; El tipo de edificio ?tipoEdi requiere el recurso ?recu para ser construido
                        (edificioRequiere ?tipoEdi ?recu)
                        ; Se dispone del recurso ?recu
                        (disponibleRecu ?recu)
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
    
)