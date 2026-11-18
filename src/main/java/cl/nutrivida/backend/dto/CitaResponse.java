package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.model.EstadoCita;
import cl.nutrivida.backend.model.ModalidadCita;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaResponse(
    Long id,
    Long usuarioId,
    Long servicioId,
    String servicioNombre,
    String nombrePaciente,
    String runPaciente,
    String emailPaciente,
    LocalDate fecha,
    LocalTime hora,
    ModalidadCita modalidad,
    EstadoCita estado
) {}
