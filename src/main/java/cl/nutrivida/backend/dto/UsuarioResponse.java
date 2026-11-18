package cl.nutrivida.backend.dto;

import cl.nutrivida.backend.model.Rol;

public record UsuarioResponse(

    Long id,
    String run,
    String nombre,
    String email,
    Rol rol,
    String region,
    String comuna,
    String direccion

) {}
