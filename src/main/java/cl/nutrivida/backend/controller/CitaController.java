package cl.nutrivida.backend.controller;

import cl.nutrivida.backend.dto.*;
import cl.nutrivida.backend.service.CitaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public List<CitaResponse> listar() {
        return citaService.listar();
    }

    @GetMapping("/mis-citas")
    @SecurityRequirement(name = "bearerAuth")
    public List<CitaResponse> misCitas(
            @AuthenticationPrincipal UserDetails usuario) {

        return citaService.misCitas(usuario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<CitaResponse> crear(
            @Valid @RequestBody CitaRequest request,
            @AuthenticationPrincipal UserDetails usuario) {

        CitaResponse creada = citaService.crear(request, usuario);

        return ResponseEntity
                .created(URI.create("/api/v1/citas/" + creada.id()))
                .body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public CitaResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequest request) {

        return citaService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public CitaResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoCitaRequest request) {

        return citaService.cambiarEstado(id, request.estado());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        citaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
