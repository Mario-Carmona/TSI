;Header and description

(define (domain ejercicio2)

    ;remove requirements that are not needed
    (:requirements :strips :typing :adl)

    (:types ;todo: enumerate types and their hierarchy here, e.g. car truck bus - vehicle
        unidad edificio localizacion - object
    )

    (:constants
        VCE - unidad
        centro_de_mando barracones extractor - edificio
        mineral gas - recurso
    )

    ; un-comment following line if constants are needed
    ;(:constants )

    (:predicates ;todo: define predicates here
        (enEdi ?edi - edificio ?x - localizacion)
        (enUni ?uni - unidad ?x - localizacion)
        (camino ?x - localizacion ?y - localizacion)
        (asignar ?recu - recurso ?x - localizacion)
        (extrayendo ?uni - unidad ?recu - recurso)
        (necesita ?edi - edificio ?recu - recurso)
        (libre ?uni - unidad)
        (construyendo ?uni - unidad ?edi - edificio)
        (genera ?edi - edificio ?recu - recurso)
    )


    (:functions ;todo: define numeric functions here
    )

    ;define actions here
    (:action navegar
        :parameters (?uni - unidad ?locaOri - localizacion ?locaDest - localizacion)
        :precondition 
            (and 
                (enUni ?uni ?locaOri)
            )
        :effect 
            (and 
                (enUni ?uni ?locaDest)
                (not (enUni ?uni ?locaOri))
            )
    )

    (:action asignar
        :parameters (?uni - unidad ?locaRecu - localizacion ?tipo - recurso)
        :precondition 
            (and 
                (asignar ?tipo ?locaRecu)
                (enUni ?uni ?locaRecu)
            )
        :effect 
            (and 
                (extrayendo ?uni ?tipo)
            )
    )
    
    (:action crear_camino
        :parameters (?x - localizacion ?y - localizacion ?z - localizacion)
        :precondition 
            (and 
                (!=
                    ?x
                    ?y
                )
                (!=
                    ?y
                    ?z
                )
                (!=
                    ?x
                    ?z
                )
                (camino ?x ?y)
                (camino ?y ?z)
            )
        :effect 
            (and 
                (camino ?x ?z)
            )
    )
    
    (:action construir
        :parameters (?uni - unidad ?edi - edificio ?loca - localizacion ?recu - recurso)
        :precondition 
            (and 
                ;(forall (?x - recurso) 
                ;    (when (and (genera ?edi ?x))
                ;        (and
                ;            (asignar ?rx ?loca)
                ;        )
                ;    )
                ;)
                (imply (genera ?edi ) (asignar gas ?loca))
                (libre ?uni)
                (enUni ?uni ?loca)
                (extrayendo VCE ?recu)
                (necesita ?edi ?recu)
            )
        :effect 
            (and 
                (construyendo ?uni ?edi)
                (enEdi ?edi ?loca)
                (not (libre ?uni))
            )
    )
)