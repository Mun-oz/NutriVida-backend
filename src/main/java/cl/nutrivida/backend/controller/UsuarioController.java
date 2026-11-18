
package cl.nutrivida.backend.controller;

import cl.nutrivida.backend.dto.*;
import cl.nutrivida.backend.service.UsuarioService;

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
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioCrearRequest request) {

        UsuarioResponse creado = usuarioService.crear(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/usuarios/" + creado.id()
                ))
                .body(creado);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActualizarRequest request,
            @AuthenticationPrincipal UserDetails administrador) {

        return usuarioService.actualizar(
                id,
                request,
                administrador.getUsername()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails administrador) {

        usuarioService.eliminar(
                id,
                administrador.getUsername()
        );

        return ResponseEntity.noContent().build();
    }
}
