package cl.nutrivida.backend.dto;

import java.util.List;

public record AuthResponse(

    String accessToken,
    String refreshToken,
    String tokenType,
    String email,
    List<String> roles

) {}
