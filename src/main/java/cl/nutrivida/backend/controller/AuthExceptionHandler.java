package cl.nutrivida.backend.controller;

import io.jsonwebtoken.JwtException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler({
        BadCredentialsException.class,
        UsernameNotFoundException.class,
        JwtException.class
    })
    public ResponseEntity<?> autenticacionInvalida(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "error", "Credenciales o token invalidos"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> datosInvalidos(
            MethodArgumentNotValidException ex) {

        return ResponseEntity
                .badRequest()
                .body(Map.of(
                    "error", "Datos de entrada invalidos"
                ));
    }
}
