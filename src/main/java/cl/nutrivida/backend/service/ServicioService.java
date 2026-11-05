package cl.nutrivida.backend.service;

import cl.nutrivida.backend.dto.ServicioRequest;
import cl.nutrivida.backend.model.Servicio;
import cl.nutrivida.backend.repository.CitaRepository;
import cl.nutrivida.backend.repository.ServicioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;

    public ServicioService(
            ServicioRepository servicioRepository,
            CitaRepository citaRepository) {
        this.servicioRepository = servicioRepository;
        this.citaRepository = citaRepository;
    }

    // Listar todos los servicios
    public List<Servicio> listar() {
        return servicioRepository.findAll();
    }

    // Obtener servicio por ID
    public Servicio obtenerPorId(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Servicio no encontrado"
                ));
    }

    // Crear servicio
    public Servicio crear(ServicioRequest request) {

        String codigo = normalizarCodigo(request.codigo());

        if (servicioRepository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El codigo del servicio ya existe"
            );
        }

        Servicio servicio = new Servicio();
        asignarDatos(servicio, request);

        return servicioRepository.save(servicio);
    }

    // Actualizar servicio
    public Servicio actualizar(Long id, ServicioRequest request) {

        Servicio servicio = obtenerPorId(id);
        String codigo = normalizarCodigo(request.codigo());

        servicioRepository.findByCodigo(codigo)
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El codigo del servicio ya existe"
                        );
                    }
                });

        asignarDatos(servicio, request);

        return servicioRepository.save(servicio);
    }

    // Eliminar servicio
    public void eliminar(Long id) {

        Servicio servicio = obtenerPorId(id);

        if (citaRepository.existsByServicio_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un servicio con citas"
            );
        }

        servicioRepository.delete(servicio);
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    // Asignar los campos del servicio
    private void asignarDatos(
            Servicio servicio,
            ServicioRequest request) {

        servicio.setCodigo(normalizarCodigo(request.codigo()));
        servicio.setNombre(request.nombre().trim());
        servicio.setDescripcion(request.descripcion());
        servicio.setDuracion(request.duracion());
        servicio.setPrecio(request.precio());
        servicio.setImagen(request.imagen());
    }
}
