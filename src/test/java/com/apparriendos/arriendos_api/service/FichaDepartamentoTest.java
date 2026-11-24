package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.model.Departamento;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Datos de la ficha (comodidades, destacados, reglas): validacion y limpieza antes de guardar. */
class FichaDepartamentoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Departamento base() {
        Departamento d = new Departamento();
        d.setNombre("Depto de prueba");
        return d;
    }

    @Test
    void normalizarQuitaRepetidosYTextosVacios() {
        Departamento d = base();
        d.setComodidades(List.of("wifi", "piscina", "wifi"));
        d.setDestacados(null);
        d.setHoraCheckOut("");
        d.setReglasAdicionales("   ");
        d.setSobreLaZona("  A 2 cuadras de la playa ");

        DepartamentoService.normalizarFicha(d);

        assertThat(d.getComodidades()).containsExactly("wifi", "piscina");
        assertThat(d.getDestacados()).isEmpty();
        assertThat(d.getHoraCheckOut()).isNull();
        assertThat(d.getReglasAdicionales()).isNull();
        assertThat(d.getSobreLaZona()).isEqualTo("A 2 cuadras de la playa");
    }

    @Test
    void fichaValidaNoTieneErrores() {
        Departamento d = base();
        d.setComodidades(List.of("wifi", "aire_acondicionado"));
        d.setDestacados(List.of("vista_al_mar", "llegada_autonoma"));
        d.setHoraCheckIn("15:00");
        d.setHoraCheckOut("");
        assertThat(validator.validate(d)).isEmpty();
    }

    @Test
    void rechazaCodigosRarosMasDeTresDestacadosYHorasMalas() {
        Departamento d = base();
        d.setComodidades(List.of("Wi Fi"));
        d.setDestacados(List.of("a", "b", "c", "d"));
        d.setHoraCheckIn("25:00");
        assertThat(validator.validate(d))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("comodidades[0].<list element>", "destacados", "horaCheckIn");
    }
}
