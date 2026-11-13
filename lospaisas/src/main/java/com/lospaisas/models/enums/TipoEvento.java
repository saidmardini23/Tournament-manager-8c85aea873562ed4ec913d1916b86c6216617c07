package com.lospaisas.models.enums;

public enum TipoEvento {
    // Goles y jugadas clave
    GOL_JUGADA,
    GOL_CABEZA,
    GOL_PENAL,
    GOL_TIRO_LIBRE,
    AUTOGOL,
    ASISTENCIA,
    PENAL_FALLADO,
    PENAL_PARADO,

    // Disciplina
    TARJETA_AMARILLA,
    DOBLE_AMARILLA,
    TARJETA_ROJA,
    EXPULSION_DIRECTA,

    // Cambios y personal
    SUSTITUCION_ENTRA,
    SUSTITUCION_SALE,
    LESION,

    // Incidencias del partido
    INICIO_PARTIDO,
    FIN_PRIMER_TIEMPO,
    INICIO_SEGUNDO_TIEMPO,
    FIN_SEGUNDO_TIEMPO,
    INICIO_TIEMPO_EXTRA,
    FIN_TIEMPO_EXTRA,
    FIN_PARTIDO,
    SUSPENSION_TEMPORAL,
    REANUDACION,
    REVISION_VAR, GOL
}