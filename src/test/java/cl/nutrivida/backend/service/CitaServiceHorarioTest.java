package cl.nutrivida.backend.service;

import cl.nutrivida.backend.dto.CitaRequest;
import cl.nutrivida.backend.model.ModalidadCita;
import cl.nutrivida.backend.repository.CitaRepository;
import cl.nutrivida.backend.repository.ServicioRepository;
import cl.nutrivida.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CitaServiceHorarioTest {

    private static final ZoneId CHILE =
            ZoneId.of("America/Santiago");

    private final CitaService service = new CitaService(
            mock(CitaRepository.class),
            mock(ServicioRepository.class),
            mock(UsuarioRepository.class)
    );

    private LocalDate proximoLunes() {
        return LocalDate.now(CHILE)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private CitaRequest crearRequest(
            LocalDate fecha,
            LocalTime hora) {

        return new CitaRequest(
                1L,
                "Paciente Prueba",
                "12.345.678-5",
                "paciente@example.com",
                fecha,
                hora,
                ModalidadCita.ONLINE
        );
    }

    private void verificarRechazo(CitaRequest request) {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.validarFechaHora(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void debeAceptarHorarioValido() {

        assertDoesNotThrow(() ->
                service.validarFechaHora(
                        crearRequest(proximoLunes(), LocalTime.of(10, 0))
                )
        );
    }

    @Test
    void debeRechazarHorarioFueraDeAtencion() {

        verificarRechazo(
                crearRequest(proximoLunes(), LocalTime.of(8, 0))
        );

        verificarRechazo(
                crearRequest(proximoLunes(), LocalTime.of(18, 0))
        );
    }

    @Test
    void debeRechazarFinDeSemana() {

        LocalDate sabado = LocalDate.now(CHILE)
                .with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

        verificarRechazo(
                crearRequest(sabado, LocalTime.of(10, 0))
        );
    }

    @Test
    void debeRechazarCitasEnElPasado() {

        LocalDate ayer = LocalDate.now(CHILE).minusDays(1);

        verificarRechazo(
                crearRequest(ayer, LocalTime.of(10, 0))
        );

        verificarRechazo(
                crearRequest(LocalDate.now(CHILE), LocalTime.MIDNIGHT)
        );
    }
}
