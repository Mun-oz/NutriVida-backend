package cl.nutrivida.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.refresh-secret}") String refreshSecret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {

        this.accessKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret));

        this.refreshKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(refreshSecret));

        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generarAccessToken(UserDetails user) {
        return generarToken(
                user.getUsername(),
                "access",
                accessKey,
                expirationMs);
    }

    public String generarRefreshToken(UserDetails user) {
        return generarToken(
                user.getUsername(),
                "refresh",
                refreshKey,
                refreshExpirationMs);
    }

    private String generarToken(
            String email,
            String tipo,
            SecretKey key,
            long duracion) {

        Date ahora = new Date();

        return Jwts.builder()
                .subject(email)
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion))
                .signWith(key)
                .compact();
    }

    public String extraerUsuarioAccess(String token) {
        return obtenerClaims(token, accessKey).getSubject();
    }

    public String extraerUsuarioRefresh(String token) {
        return obtenerClaims(token, refreshKey).getSubject();
    }

    public boolean esAccessValido(String token, UserDetails user) {
        return esValido(token, user, accessKey, "access");
    }

    public boolean esRefreshValido(String token, UserDetails user) {
        return esValido(token, user, refreshKey, "refresh");
    }

    private boolean esValido(
            String token,
            UserDetails user,
            SecretKey key,
            String tipo) {

        Claims claims = obtenerClaims(token, key);

        return user.getUsername().equals(claims.getSubject())
                && tipo.equals(claims.get("tipo", String.class))
                && claims.getExpiration().after(new Date());
    }

    private Claims obtenerClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
