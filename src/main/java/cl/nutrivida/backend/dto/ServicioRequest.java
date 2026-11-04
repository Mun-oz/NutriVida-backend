package cl.nutrivida.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ServicioRequest(

        @NotBlank
        @Size(max = 20)
        String codigo,

        @NotBlank
        @Size(max = 150)
        String nombre,

        String descripcion,

        @Size(max = 50)
        String duracion,

        @NotNull
        @PositiveOrZero
        Integer precio,

        @Size(max = 255)
        String imagen

) {
}
