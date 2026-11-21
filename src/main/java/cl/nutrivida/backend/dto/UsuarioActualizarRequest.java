package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.model.Rol;
import cl.nutrivida.backend.validation.RunValido;
import jakarta.validation.constraints.*;

public record UsuarioActualizarRequest(

    @NotBlank
    @Size(max = 150)
    String nombre,

    @NotBlank
    @Email
    String email,

    @NotNull
    Rol rol,

    @Size(min = 8, max = 72)
    String password,

    @Size(max = 15)
    @RunValido(required = false)
    String run,

    @Size(max = 100)
    String region,

    @Size(max = 100)
    String comuna,

    @Size(max = 200)
    String direccion

) {}
