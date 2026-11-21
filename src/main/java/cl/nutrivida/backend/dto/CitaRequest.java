package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.model.ModalidadCita;
import cl.nutrivida.backend.validation.RunValido;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(

    @NotNull
    @Positive
    Long servicioId,

    @NotBlank
    @Size(max = 150)
    String nombrePaciente,

    @Size(max = 15)
    @RunValido
    String runPaciente,

    @NotBlank
    @Email
    @Size(max = 150)
    String emailPaciente,

    @NotNull
    @FutureOrPresent
    LocalDate fecha,

    @NotNull
    LocalTime hora,

    @NotNull
    ModalidadCita modalidad

) {}

