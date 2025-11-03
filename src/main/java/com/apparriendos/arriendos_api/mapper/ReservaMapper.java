package com.apparriendos.arriendos_api.mapper;

import com.apparriendos.arriendos_api.model.Reserva;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReservaMapper {
    List<Reserva> findAll();
    Reserva findById(@Param("id") Long id);

    // inserta y deja el id en la propiedad (usando selectKey en XML)
    void insert(Reserva reserva);

    void update(Reserva reserva);
    void delete(@Param("id") Long id);

    // devuelve conteo de reservas solapadas para determinada depto y rango
    int countOverlapping(
        @Param("idDepartamento") Long idDepartamento,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
}
