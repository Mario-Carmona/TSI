(define (domain ejercicio2)
    (:requirements :strips :typing :adl)
    (:types
        tipo_unidad tipo_edificio entidad localizacion recurso - object
        unidad edificio - entidad
    )
    (:constants
        VCE - tipo_unidad
        Centro_de_mando Barracones Extractor - tipo_edificio
        Mineral Gas - recurso
    )
    (:predicates
        (edificioEs ?edi - edificio ?tipo - tipo_edificio)
        ; El edificio ?edi se encuentra en la localización ?x
        (edificioEn ?edi - edificio ?x - localizacion)
        (unidadEs ?uni - unidad ?tipo - tipo_unidad)
        ; La unidad ?uni se encuentra en la localización ?x
        (unidadEn ?uni - unidad ?x - localizacion)
        ; Existe un camino desde la localización ?x hasta la localización ?y
        (camino ?x - localizacion ?y - localizacion)
        ; Un depósito del recurso ?recu se encuentra en la localización ?x
        (depositoEn ?recu - recurso ?x - localizacion)
        ; La unidad ?vce está extrayendo el recurso ?recu
        (extrayendo ?uni - unidad ?recu - recurso)
        (libre ?uni - unidad)
        (edificioRequiere ?edi - edificio ?recu - recurso)
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
                (unidadEs ?uni VCE)
                (libre ?uni)
                (unidadEn ?uni ?loca)
                (or 
                    (depositoEn Mineral ?loca)
                    (and
                        (depositoEn Gas ?loca)
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
                (when (depositoEn Gas ?loca) 
                    (extrayendo ?uni Gas)
                )
                (when (depositoEn Mineral ?loca) 
                    (extrayendo ?uni Mineral)
                )
                (not (libre ?uni))
            )
    )

    (:action construir
        :parameters (?uni - unidad ?edi - edificio ?loca - localizacion)
        :precondition 
            (and 
                (not (construido ?edi))
                (unidadEs ?uni VCE)
                (libre ?uni)
                (unidadEn ?uni ?loca)

                (exists (?recu - recurso)
                    (and
                        (edificioRequiere ?edi ?recu)
                        (exists (?otroVCE - unidad)
                            (and
                                (extrayendo ?otroVCE ?recu)
                            )
                        )
                    )
                )
                
            )
        :effect 
            (and 
                (edificioEn ?edi ?loca)
                (construido ?edi)
            )
    )
)