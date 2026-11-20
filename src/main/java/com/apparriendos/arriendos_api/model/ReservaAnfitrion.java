package com.apparriendos.arriendos_api.model;

import lombok.Data;

import java.time.LocalDate;

/** Reserva vista por el anfitrion: incluye a que propiedad corresponde y quien reserva. */
@Data
public class ReservaAnfitrion {
    private Long idReserva;
    private Long idDepartamento;
    private String nombreDepartamento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String nombreHuesped;
    private String emailHuesped;
    private String telefonoHuesped;
}
