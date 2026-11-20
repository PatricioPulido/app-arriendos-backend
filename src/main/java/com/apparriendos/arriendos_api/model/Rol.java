package com.apparriendos.arriendos_api.model;

public enum Rol {
    ADMIN,
    /** Huesped: busca y reserva alojamientos. */
    ARRENDATARIO,
    /** Publica alojamientos. Tambien puede reservar como huesped. */
    ANFITRION
}
