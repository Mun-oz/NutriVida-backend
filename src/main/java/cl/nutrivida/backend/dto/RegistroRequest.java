package cl.nutrivida.backend.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(

    @NotBlank
    @Pattern(regexp = "^[0-9]{7,8}[0-9kK]$")
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
