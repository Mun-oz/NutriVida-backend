package cl.nutrivida.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank
    @Email
    @Size(max = 150)
    String email,

    @NotBlank
    String password

) {}
