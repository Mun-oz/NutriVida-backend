package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.model.ModalidadCita;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(

    @NotNull
    Long servicioId,

    @NotBlank
    @Size(max = 150)
    String nombrePaciente,

    @NotBlank
    @Size(max = 15)
    String runPaciente,

    @NotBlank
    @Email
    String emailPaciente,

    @NotNull
    @FutureOrPresent
    LocalDate fecha,

    @NotNull
    LocalTime hora,

    @NotNull
    ModalidadCita modalidad

) {}
