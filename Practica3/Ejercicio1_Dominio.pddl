;Header and description

(define (domain domain_name)

    ;remove requirements that are not needed
    (:requirements :strips :typing :adl)

    (:types ;todo: enumerate types and their hierarchy here, e.g. car truck bus - vehicle
        unidad edificio localizacion - object
    )

    (:constants
        VCE - unidad
        centro_de_mando barracones - edificio
        mineral gas - recurso
    )

    ; un-comment following line if constants are needed
    ;(:constants )

    (:predicates ;todo: define predicates here
        (enEdi ?edi - edificio ?x - localizacion)
        (enUni ?uni - unidad ?x - localizacion)
        (camino ?x - localizacion ?y - localizacion)
        (construido ?edi - edificio)
        (asignar ?recu - recurso ?x - localizacion)
        (extrayendo ?vce - VCE ?recu - recurso)
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
    
)