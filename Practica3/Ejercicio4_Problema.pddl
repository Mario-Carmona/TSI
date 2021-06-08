(define (problem ejercicio4) 
    (:domain ejercicio4)
    (:objects 
        ; Unidades
        VCE1 - unidad
        VCE2 - unidad
        VCE3 - unidad
        Marine1 - unidad
        Marine2 - unidad
        Segador1 - unidad

        ; Edificios
        CentroDeMando1 - edificio
        Extractor1 - edificio
        Barracones1 - edificio
        
        ; Localizaciones
        LOC11 LOC12 LOC13 LOC14 - localizacion
        LOC21 LOC22 LOC23 LOC24 - localizacion
        LOC31 LOC32 LOC33 LOC34 - localizacion
    )
    (:init
        ; Caminos
        ;   LOC11
        (camino LOC11 LOC12)
        (camino LOC11 LOC21)
        ;   LOC12
        (camino LOC12 LOC11)
        (camino LOC12 LOC22)
        ;   LOC13
        (camino LOC13 LOC14)
        (camino LOC13 LOC23)
        ;   LOC14
        (camino LOC14 LOC13)
        (camino LOC14 LOC24)
        ;   LOC21
        (camino LOC21 LOC11)
        (camino LOC21 LOC31)
        ;   LOC22
        (camino LOC22 LOC12)
        (camino LOC22 LOC32)
        (camino LOC22 LOC23)
        ;   LOC23
        (camino LOC23 LOC22)
        (camino LOC23 LOC13)
        ;   LOC24
        (camino LOC24 LOC14)
        (camino LOC24 LOC34)
        ;   LOC31
        (camino LOC31 LOC21)
        (camino LOC31 LOC32)
        ;   LOC32
        (camino LOC32 LOC22)
        (camino LOC32 LOC31)
        ;   LOC33
        (camino LOC33 LOC34)
        ;   LOC34
        (camino LOC34 LOC24)
        (camino LOC34 LOC33)

        ; Localización de edificios
        (edificioEn CentroDeMando1 LOC11)
        (ocupadaLoca LOC11)

        ; Localización de unidades
        (unidadEn VCE1 LOC11)

        ; Localización de recursos
        (depositoEn Mineral LOC23)
        (depositoEn Mineral LOC33)
        (depositoEn Gas_vespeno LOC13)

        ; Tipos de los edificios
        (edificioEs CentroDeMando1 Centro_de_mando)
        (edificioEs Extractor1 Extractor)
        (edificioEs Barracones1 Barracones)

        ; Tipos de las unidades
        (unidadEs VCE1 VCE)
        (unidadEs VCE2 VCE)
        (unidadEs VCE3 VCE)
        (unidadEs Marine1 Marine)
        (unidadEs Marine2 Marine)
        (unidadEs Segador1 Segador)

        ; Unidades libres
        (libre VCE1)

        ; Unidades reclutadas
        (reclutada VCE1)

        ; Edificios construidos
        (construido CentroDeMando1)

        ; Recurso requerido por un edificio para su construcción
        (edificioRequiere Extractor Mineral)
        (edificioRequiere Barracones Mineral)
        (edificioRequiere Barracones Gas_vespeno)

        ; Recurso requerido por una unidad para su reclutamiento
        (unidadRequiereRecu VCE Mineral)
        (unidadRequiereRecu Marine Mineral)
        (unidadRequiereRecu Segador Mineral)
        (unidadRequiereRecu Segador Gas_vespeno)

        ; Edificio requerido por una unidad para su reclutamiento
        (unidadRequiereEdi VCE Centro_de_mando)
        (unidadRequiereEdi Marine Barracones)
        (unidadRequiereEdi Segador Barracones)
    )
    (:goal 
        (and
            (edificioEn Barracones1 LOC32)
            (unidadEn Marine1 LOC31)
            (unidadEn Marine2 LOC24)
            (unidadEn Segador1 LOC12)
        )
    )
)
