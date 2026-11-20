package cl.nutrivida.backend.controller;

import cl.nutrivida.backend.dto.RegistroRequest;
import cl.nutrivida.backend.model.Usuario;
import cl.nutrivida.backend.repository.UsuarioRepository;
import cl.nutrivida.backend.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthControllerRunTest {

    private UsuarioRepository usuarioRepository;
    private AuthController authController;

    @BeforeEach
    void configurar() {

        usuarioRepository = mock(UsuarioRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("password-cifrada");

        authController = new AuthController(
                mock(AuthenticationManager.class),
                mock(UserDetailsService.class),
                mock(JwtService.class),
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void debeGuardarRunNormalizado() {

        RegistroRequest request = crearRegistro(
                "10.000.013-k",
                "paciente1@example.com"
        );

        ResponseEntity<?> respuesta =
                authController.registrar(request);

        assertEquals(201, respuesta.getStatusCode().value());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        assertEquals(
                "10000013K",
                captor.getValue().getRun()
        );

        verify(usuarioRepository)
                .existsByRun("10000013K");
    }

    @Test
    void debeRechazarRunDuplicadoConOtroFormato() {

        when(usuarioRepository.existsByRun("10000013K"))
                .thenReturn(false, true);

        ResponseEntity<?> primeraRespuesta =
                authController.registrar(
                        crearRegistro(
                                "10.000.013-K",
                                "paciente1@example.com"
                        )
                );

        ResponseEntity<?> segundaRespuesta =
                authController.registrar(
                        crearRegistro(
                                "10000013K",
                                "paciente2@example.com"
                        )
                );

        assertEquals(
                201,
                primeraRespuesta.getStatusCode().value()
        );

        assertEquals(
                409,
                segundaRespuesta.getStatusCode().value()
        );

        verify(usuarioRepository, times(1))
                .save(any(Usuario.class));

        verify(usuarioRepository, times(2))
                .existsByRun("10000013K");
    }

    private RegistroRequest crearRegistro(
            String run,
            String email) {

        return new RegistroRequest(
                run,
                "Paciente Prueba",
                email,
                "ClaveSegura123",
                "Metropolitana",
                "Santiago",
                "Direccion 123"
        );
    }
}
