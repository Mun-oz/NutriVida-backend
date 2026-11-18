package cl.nutrivida.backend.service;

import cl.nutrivida.backend.dto.*;
import cl.nutrivida.backend.model.*;
import cl.nutrivida.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

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
                cita.setRunPaciente(usuario.getRun());
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
            EstadoCita estado) {

        Cita cita = buscar(id);
        cita.setEstado(estado);

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
                request.runPaciente().trim().toUpperCase(Locale.ROOT)
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
}
