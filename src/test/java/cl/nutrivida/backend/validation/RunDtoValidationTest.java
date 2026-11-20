package cl.nutrivida.backend.validation;

import cl.nutrivida.backend.dto.CitaRequest;
import cl.nutrivida.backend.dto.RegistroRequest;
import cl.nutrivida.backend.dto.UsuarioCrearRequest;
import cl.nutrivida.backend.dto.UsuarioActualizarRequest;
import cl.nutrivida.backend.model.ModalidadCita;
import cl.nutrivida.backend.model.Rol;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RunDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    // REGISTRO DE PACIENTES

    @Test
    void registroDebeAceptarRunValido() {
        RegistroRequest request = crearRegistro("12.345.678-5");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void registroDebeRechazarRunInvalido() {
        RegistroRequest request = crearRegistro("12.345.678-4");

        assertTrue(tieneErrorRun(request, "run"));
    }

    @Test
    void registroDebeRechazarRunNulo() {
        RegistroRequest request = crearRegistro(null);

        assertTrue(tieneErrorRun(request, "run"));
    }

    // RESERVAS DE CITAS

    @Test
    void citaDebeAceptarRunValido() {
        CitaRequest request = crearCita("10.000.013-K");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void citaDebeRechazarRunInvalido() {
        CitaRequest request = crearCita("12.345.678-4");

        assertTrue(tieneErrorRun(request, "runPaciente"));
    }

    @Test
    void citaDebeRechazarRunNulo() {
        CitaRequest request = crearCita(null);

        assertTrue(tieneErrorRun(request, "runPaciente"));
    }

    // CREACION ADMINISTRATIVA

    @Test
    void crearUsuarioDebeAceptarRunOpcional() {
        UsuarioCrearRequest request = new UsuarioCrearRequest(
                "Usuario Prueba",
                "usuario@example.com",
                "ClaveSegura123",
                Rol.PACIENTE,
                null,
                "Metropolitana",
                "Santiago",
                "Direccion 123"
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void crearUsuarioDebeRechazarRunInvalido() {
        UsuarioCrearRequest request = new UsuarioCrearRequest(
                "Usuario Prueba",
                "usuario@example.com",
                "ClaveSegura123",
                Rol.PACIENTE,
                "12.345.678-4",
                "Metropolitana",
                "Santiago",
                "Direccion 123"
        );

        assertTrue(tieneErrorRun(request, "run"));
    }

    // ACTUALIZACION ADMINISTRATIVA

    @Test
    void actualizarUsuarioDebeAceptarRunOpcional() {
        UsuarioActualizarRequest request =
                crearActualizacion(null);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void actualizarUsuarioDebeRechazarRunInvalido() {
        UsuarioActualizarRequest request =
                crearActualizacion("12.345.678-4");

        assertTrue(tieneErrorRun(request, "run"));
    }

    // METODOS AUXILIARES

    private RegistroRequest crearRegistro(String run) {
        return new RegistroRequest(
                run,
                "Paciente Prueba",
                "paciente@example.com",
                "ClaveSegura123",
                "Metropolitana",
                "Santiago",
                "Direccion 123"
        );
    }

    private CitaRequest crearCita(String run) {
        return new CitaRequest(
                1L,
                "Paciente Prueba",
                run,
                "paciente@example.com",
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0),
                ModalidadCita.ONLINE
        );
    }

    private UsuarioActualizarRequest crearActualizacion(
            String run) {

        return new UsuarioActualizarRequest(
                "Usuario Prueba",
                "usuario@example.com",
                Rol.PACIENTE,
                null,
                run,
                "Metropolitana",
                "Santiago",
                "Direccion 123"
        );
    }

    private boolean tieneErrorRun(
            Object request,
            String campo) {

        return validator.validate(request)
                .stream()
                .anyMatch(error ->
                        error.getPropertyPath()
                                .toString()
                                .equals(campo)
                );
    }
}
