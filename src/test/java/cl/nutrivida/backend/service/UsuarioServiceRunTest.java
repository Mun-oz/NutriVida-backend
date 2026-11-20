package cl.nutrivida.backend.service;

import cl.nutrivida.backend.dto.UsuarioCrearRequest;
import cl.nutrivida.backend.dto.UsuarioActualizarRequest;
import cl.nutrivida.backend.dto.UsuarioResponse;
import cl.nutrivida.backend.model.Rol;
import cl.nutrivida.backend.model.Usuario;
import cl.nutrivida.backend.repository.CitaRepository;
import cl.nutrivida.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsuarioServiceRunTest {

    private UsuarioRepository usuarios;
    private UsuarioService service;

    @BeforeEach
    void configurar() {

        usuarios = mock(UsuarioRepository.class);

        PasswordEncoder encoder = mock(PasswordEncoder.class);

        when(encoder.encode(anyString()))
                .thenReturn("password-cifrada");

        when(usuarios.save(any(Usuario.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        service = new UsuarioService(
                usuarios,
                mock(CitaRepository.class),
                encoder
        );
    }

    @Test
    void debeNormalizarRunAlCrearUsuario() {

        UsuarioResponse respuesta = service.crear(
                crearRequest("10.000.013-k")
        );

        assertEquals("10000013K", respuesta.run());

        verify(usuarios).findByRun("10000013K");
    }

    @Test
    void debeAceptarRunOpcionalAlCrearUsuario() {

        UsuarioResponse respuesta = service.crear(
                crearRequest(null)
        );

        assertNull(respuesta.run());

        verify(usuarios, never()).findByRun(anyString());
    }

    @Test
    void debeNormalizarRunAlActualizarUsuario() {

        Usuario existente = new Usuario();

        existente.setNombre("Paciente Anterior");
        existente.setEmail("anterior@example.com");
        existente.setRol(Rol.PACIENTE);

        when(usuarios.findById(7L))
                .thenReturn(Optional.of(existente));

        UsuarioActualizarRequest request =
                new UsuarioActualizarRequest(
                        "Paciente Actualizado",
                        "actualizado@example.com",
                        Rol.PACIENTE,
                        null,
                        "10.000.013-k",
                        "Metropolitana",
                        "Santiago",
                        "Direccion 123"
                );

        UsuarioResponse respuesta = service.actualizar(
                7L,
                request,
                "admin@example.com"
        );

        assertEquals("10000013K", respuesta.run());

        verify(usuarios).findByRun("10000013K");
    }

    @Test
    void debeRechazarRunDuplicadoConOtroFormato() {

        Usuario otroUsuario = mock(Usuario.class);

        when(otroUsuario.getId()).thenReturn(99L);

        when(usuarios.findByRun("10000013K"))
                .thenReturn(Optional.of(otroUsuario));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(
                        crearRequest("10.000.013-K")
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode()
        );

        verify(usuarios, never()).save(any(Usuario.class));
    }

    private UsuarioCrearRequest crearRequest(String run) {

        return new UsuarioCrearRequest(
                "Paciente Prueba",
                "paciente@example.com",
                "ClaveSegura123",
                Rol.PACIENTE,
                run,
                "Metropolitana",
                "Santiago",
                "Direccion 123"
        );
    }
}
