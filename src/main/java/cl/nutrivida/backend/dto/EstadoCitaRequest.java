package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.model.EstadoCita;
import jakarta.validation.constraints.NotNull;

public record EstadoCitaRequest(
    @NotNull EstadoCita estado
) {}
