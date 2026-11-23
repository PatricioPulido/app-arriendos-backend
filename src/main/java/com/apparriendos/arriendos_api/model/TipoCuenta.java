package com.apparriendos.arriendos_api.model;

/** Lo que elige la persona al registrarse. */
public enum TipoCuenta {
    /** Quiere reservar: queda con rol ARRENDATARIO. */
    HUESPED,
    /** Quiere publicar su alojamiento: queda con rol ANFITRION. */
    ANFITRION;

    public Rol rol() {
        return this == ANFITRION ? Rol.ANFITRION : Rol.ARRENDATARIO;
    }
}
