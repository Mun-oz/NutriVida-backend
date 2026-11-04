
package cl.nutrivida.backend.controller;

import cl.nutrivida.backend.dto.ServicioRequest;
import cl.nutrivida.backend.model.Servicio;
import cl.nutrivida.backend.service.ServicioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<Servicio> listar() {
        return servicioService.listar();
    }

    @GetMapping("/{id}")
    public Servicio obtener(@PathVariable Long id) {
        return servicioService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<Servicio> crear(
            @Valid @RequestBody ServicioRequest request) {

        Servicio creado = servicioService.crear(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/servicios/" + creado.getId()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public Servicio actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicioRequest request) {

        return servicioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        servicioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
