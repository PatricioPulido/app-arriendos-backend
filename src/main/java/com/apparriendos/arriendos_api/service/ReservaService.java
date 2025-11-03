package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.mapper.ReservaMapper;
import com.apparriendos.arriendos_api.mapper.DepartamentoMapper;
import com.apparriendos.arriendos_api.mapper.ArrendatarioMapper;
import com.apparriendos.arriendos_api.model.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Service
public class ReservaService {
    private final ReservaMapper mapper;
    private final DepartamentoMapper deptMapper;
    private final ArrendatarioMapper arrMapper;
    private final DbErrorHandler dbErrorHandler;
    private final Logger logger = LoggerFactory.getLogger(ReservaService.class);

    public ReservaService(ReservaMapper mapper,
                          DepartamentoMapper deptMapper,
                          ArrendatarioMapper arrMapper,
                          DbErrorHandler dbErrorHandler) {
        this.mapper = mapper;
        this.deptMapper = deptMapper;
        this.arrMapper = arrMapper;
        this.dbErrorHandler = dbErrorHandler;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Reserva> list = mapper.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error listar reservas", ex);
            return dbErrorHandler.handleException(ex, "Listar reservas");
        }
    }

    public ResponseEntity<?> getById(Long id) {
        try {
            Reserva r = mapper.findById(id);
            if (r == null) return ResponseEntity.status(404).body(Map.of("mensaje", "Reserva no encontrada"));
            return ResponseEntity.ok(r);
        } catch (Exception ex) {
            logger.error("Error getById reserva", ex);
            return dbErrorHandler.handleException(ex, "Obtener reserva");
        }
    }

    @Transactional
    public ResponseEntity<?> create(Reserva reserva) {
        try {
            // validaciones básicas
            if (reserva.getIdDepartamento() == null || reserva.getIdArrendatario() == null)
                return ResponseEntity.badRequest().body(Map.of("mensaje", "idDepartamento e idArrendatario son obligatorios"));

            if (reserva.getFechaInicio() == null || reserva.getFechaFin() == null)
                return ResponseEntity.badRequest().body(Map.of("mensaje", "Fechas de inicio y fin obligatorias"));

            if (!reserva.getFechaInicio().isBefore(reserva.getFechaFin()))
                return ResponseEntity.badRequest().body(Map.of("mensaje", "fechaInicio debe ser anterior a fechaFin"));

            // comprobar existencia de departamento y arrendatario
            if (deptMapper.findById(reserva.getIdDepartamento()) == null)
                return ResponseEntity.status(400).body(Map.of("mensaje", "Departamento no existe"));

            if (arrMapper.findById(reserva.getIdArrendatario()) == null)
                return ResponseEntity.status(400).body(Map.of("mensaje", "Arrendatario no existe"));

            // comprobar disponibilidad
            int overlaps = mapper.countOverlapping(reserva.getIdDepartamento(), reserva.getFechaInicio(), reserva.getFechaFin());
            if (overlaps > 0) {
                return ResponseEntity.status(409).body(Map.of("mensaje", "Fechas no disponibles"));
            }

            // set estado por defecto si null
            if (reserva.getEstado() == null) reserva.setEstado("PENDIENTE");

            // insertar (selectKey asigna id)
            mapper.insert(reserva);

            return ResponseEntity.status(201).body(Map.of("mensaje", "Reserva creada", "id", reserva.getIdReserva()));
        } catch (Exception ex) {
            logger.error("Error crear reserva", ex);
            return dbErrorHandler.handleException(ex, "Crear reserva");
        }
    }

    @Transactional
    public ResponseEntity<?> update(Long id, Reserva reserva) {
        try {
            reserva.setIdReserva(id);
            mapper.update(reserva);
            return ResponseEntity.ok(Map.of("mensaje", "Reserva actualizada"));
        } catch (Exception ex) {
            logger.error("Error actualizar reserva", ex);
            return dbErrorHandler.handleException(ex, "Actualizar reserva");
        }
    }

    public ResponseEntity<?> delete(Long id) {
        try {
            mapper.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Reserva eliminada"));
        } catch (Exception ex) {
            logger.error("Error eliminar reserva", ex);
            return dbErrorHandler.handleException(ex, "Eliminar reserva");
        }
    }
}
