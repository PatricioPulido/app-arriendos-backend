package com.apparriendos.arriendos_api.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class Arrendatario {
    private Long idArrendatario;         // wrapper, puede ser null antes del INSERT

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String apellido;

    @Email(message = "Email inválido")
    private String email;

    // teléfono como String para permitir +, 0 iniciales, guiones, etc.
    private String telefono;

    // usar LocalDate; MyBatis / JDBC 4.2+ lo mapean bien. Si no, usa java.sql.Date.
    private LocalDate fechaNacimiento;

    // opción 1 (recomendada): solo FK (id)
    private Long idDepartamento;

    // opción 2 (si quiero la entidad embebida): descomentar y mapear con resultMap/association
    // private Departamento departamento;
}
