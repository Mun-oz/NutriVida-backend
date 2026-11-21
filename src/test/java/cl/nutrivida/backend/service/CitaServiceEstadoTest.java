package cl.nutrivida.backend.service;

import cl.nutrivida.backend.model.*;
import cl.nutrivida.backend.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CitaServiceEstadoTest {

    private final CitaRepository citas =
            mock(CitaRepository.class);

    private final CitaService service = new CitaService(
            citas,
            mock(ServicioRepository.class),
            mock(UsuarioRepository.class)
    );

    @Test
    void debeConfirmarCitaPendiente() {

        Cita cita = new Cita();
        cita.setEstado(EstadoCita.PENDIENTE);

        Servicio servicio = new Servicio();
        servicio.setNombre("Consulta");
        cita.setServicio(servicio);

        when(citas.findById(1L))
                .thenReturn(Optional.of(cita));

        when(citas.save(any(Cita.class)))
                .thenAnswer(i -> i.getArgument(0));

        service.cambiarEstado(1L, EstadoCita.CONFIRMADA);

        assertEquals(
                EstadoCita.CONFIRMADA,
                cita.getEstado()
        );

        verify(citas).save(cita);
    }

    @Test
    void debeRechazarReactivacionDeCitaCancelada() {

        Cita cita = new Cita();
        cita.setEstado(EstadoCita.CANCELADA);

        when(citas.findById(1L))
                .thenReturn(Optional.of(cita));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstado(
                        1L,
                        EstadoCita.CONFIRMADA
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                error.getStatusCode()
        );

        verify(citas, never()).save(any(Cita.class));
    }
}
