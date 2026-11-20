package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.validation.RunValido;
import jakarta.validation.constraints.*;

public record RegistroRequest(

    @RunValido
    @Size(max = 15)
    String run,

    @NotBlank
    @Size(max = 150)
    String nombre,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8, max = 72)
    String password,

    @NotBlank
    String region,

    @NotBlank
    String comuna,

    @NotBlank
    String direccion

) {}

