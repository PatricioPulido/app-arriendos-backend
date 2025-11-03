package com.apparriendos.arriendos_api.model;

import lombok.Data;
import java.time.LocalDate;
import com.apparriendos.arriendos_api.model.Departamento;
import com.apparriendos.arriendos_api.model.Arrendatario;


@Data
public class Reserva {
    private Long idReserva;
    private Long idDepartamento;
    private Long idArrendatario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado; // "PENDIENTE", "CONFIRMADA", "CANCELADA", "FINALIZADA"

    // opcional: para respuestas con join
    private Departamento departamento;
    private Arrendatario arrendatario;
}
