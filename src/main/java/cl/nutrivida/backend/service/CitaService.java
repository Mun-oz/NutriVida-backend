package cl.nutrivida.backend.service;

import cl.nutrivida.backend.dto.*;
import cl.nutrivida.backend.model.*;
import cl.nutrivida.backend.repository.*;
import cl.nutrivida.backend.validation.RunUtils;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class CitaService {

    private final CitaRepository citas;
    private final ServicioRepository servicios;
    private final UsuarioRepository usuarios;

    public CitaService(
            CitaRepository citas,
            ServicioRepository servicios,
            UsuarioRepository usuarios) {
        this.citas = citas;
        this.servicios = servicios;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listar() {
        return citas.findAll().stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public CitaResponse obtener(Long id) {
        return convertir(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> misCitas(UserDetails principal) {

        Usuario usuario = usuarios.findByEmail(
                principal.getUsername()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado"
        ));

        return citas.findByUsuario_Id(usuario.getId())
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional
    public CitaResponse crear(
            CitaRequest request,
            UserDetails principal) {
                 validarFechaHora(request);

        Servicio servicio = buscarServicio(request.servicioId());

        validarDisponibilidad(request, null);

        Cita cita = new Cita();

        cita.setServicio(servicio);
        asignarDatos(cita, request);

        if (principal != null) {
            Usuario usuario = usuarios.findByEmail(
                    principal.getUsername()
            ).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no encontrado"
            ));

            if (usuario.getRol() == Rol.PACIENTE) {
                if (usuario.getRun() == null ||
                        usuario.getRun().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El paciente debe tener un RUN registrado"
                    );
                }

                cita.setUsuario(usuario);
                cita.setNombrePaciente(usuario.getNombre());
                cita.setRunPaciente(
                RunUtils.normalizar(usuario.getRun())
                );
                cita.setEmailPaciente(usuario.getEmail());
            }
        }

        cita.setEstado(EstadoCita.PENDIENTE);

        return convertir(citas.save(cita));
    }

    @Transactional
    public CitaResponse actualizar(
            Long id,
            CitaRequest request) {

            validarFechaHora(request);
            

        Cita cita = buscar(id);
        Servicio servicio = buscarServicio(request.servicioId());

        if (cita.getEstado() != EstadoCita.CANCELADA) {
            validarDisponibilidad(request, id);
        }

        cita.setServicio(servicio);
        asignarDatos(cita, request);

        return convertir(citas.save(cita));
    }

    @Transactional
    public CitaResponse cambiarEstado(
            Long id,
            EstadoCita nuevoEstado) {

        Cita cita = buscar(id);
        EstadoCita actual = cita.getEstado();

        if (actual == nuevoEstado) {
            return convertir(cita);
        }

        boolean permitido = switch (actual) {
            case PENDIENTE ->
                    nuevoEstado == EstadoCita.CONFIRMADA ||
                    nuevoEstado == EstadoCita.CANCELADA;

            case CONFIRMADA ->
                    nuevoEstado == EstadoCita.COMPLETADA ||
                    nuevoEstado == EstadoCita.CANCELADA;

            case CANCELADA, COMPLETADA -> false;
        };

        if (!permitido) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cambio de estado no permitido"
            );
        }

        cita.setEstado(nuevoEstado);

        return convertir(citas.save(cita));
    }

    @Transactional
    public void eliminar(Long id) {
        Cita cita = buscar(id);
        citas.delete(cita);
    }

    private Cita buscar(Long id) {
        return citas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cita no encontrada"
                ));
    }

    private Servicio buscarServicio(Long id) {
        return servicios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Servicio no encontrado"
                ));
    }


    void validarFechaHora(CitaRequest request) {

        LocalDate fecha = request.fecha();
        LocalTime hora = request.hora();

        if (fecha == null || hora == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha y hora son obligatorias"
            );
        }

        ZonedDateTime ahora = ZonedDateTime.now(ZONA_CHILE);

        // Rechazar fechas y horas pasadas.
        if (fecha.isBefore(ahora.toLocalDate()) ||
                (fecha.isEqual(ahora.toLocalDate()) &&
                        !hora.isAfter(ahora.toLocalTime()))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede reservar una cita en el pasado"
            );
        }

        // Solo permitir lunes a viernes.
        DayOfWeek dia = fecha.getDayOfWeek();

        if (dia == DayOfWeek.SATURDAY ||
                dia == DayOfWeek.SUNDAY) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se permiten citas durante fines de semana"
            );
        }

        // Validar horario de atención.
        if (hora.isBefore(HORA_APERTURA) ||
                !hora.isBefore(HORA_CIERRE)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El horario de atención es de 09:00 a 18:00"
            );
        }
    }


    private void validarDisponibilidad(
            CitaRequest request,
            Long idActual) {

        Long servicioId = request.servicioId();
        LocalDate fecha = request.fecha();
        LocalTime hora = request.hora();

        boolean ocupado;

        if (idActual == null) {
            ocupado =
                citas.existsByServicio_IdAndFechaAndHoraAndEstadoNot(
                    servicioId,
                    fecha,
                    hora,
                    EstadoCita.CANCELADA
                );
        } else {
            ocupado =
                citas.existsByServicio_IdAndFechaAndHoraAndEstadoNotAndIdNot(
                    servicioId,
                    fecha,
                    hora,
                    EstadoCita.CANCELADA,
                    idActual
                );
        }

        if (ocupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El horario ya esta reservado"
            );
        }
    }

    private void asignarDatos(Cita cita, CitaRequest request) {

        cita.setNombrePaciente(request.nombrePaciente().trim());

        cita.setRunPaciente(
                RunUtils.normalizar(request.runPaciente())
        );

        cita.setEmailPaciente(
                request.emailPaciente().trim().toLowerCase(Locale.ROOT)
        );

        cita.setFecha(request.fecha());
        cita.setHora(request.hora());
        cita.setModalidad(request.modalidad());
    }

    private CitaResponse convertir(Cita cita) {

        Long usuarioId = cita.getUsuario() == null
                ? null
                : cita.getUsuario().getId();

        return new CitaResponse(
                cita.getId(),
                usuarioId,
                cita.getServicio().getId(),
                cita.getServicio().getNombre(),
                cita.getNombrePaciente(),
                cita.getRunPaciente(),
                cita.getEmailPaciente(),
                cita.getFecha(),
                cita.getHora(),
                cita.getModalidad(),
                cita.getEstado()
        );
    }

    private static final ZoneId ZONA_CHILE =
        ZoneId.of("America/Santiago");

    private static final LocalTime HORA_APERTURA =
            LocalTime.of(9, 0);

    private static final LocalTime HORA_CIERRE =
            LocalTime.of(18, 0);
}
