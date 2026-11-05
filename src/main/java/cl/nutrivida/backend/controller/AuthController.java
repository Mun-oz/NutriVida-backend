package cl.nutrivida.backend.controller;

import cl.nutrivida.backend.dto.*;
import cl.nutrivida.backend.model.Rol;
import cl.nutrivida.backend.model.Usuario;
import cl.nutrivida.backend.repository.UsuarioRepository;
import cl.nutrivida.backend.security.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authManager,
            UserDetailsService userDetailsService,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(
            @Valid @RequestBody RegistroRequest request) {

        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        String run = request.run().trim()
                .toUpperCase(Locale.ROOT);

        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Correo ya registrado"));
        }

        if (usuarioRepository.existsByRun(run)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "RUN ya registrado"));
        }

        Usuario usuario = new Usuario();

        usuario.setRun(run);
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(
                passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.PACIENTE);
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "mensaje", "Paciente registrado correctamente"
                ));
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        UserDetails user =
                userDetailsService.loadUserByUsername(email);

        return crearRespuesta(user);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @Valid @RequestBody RefreshRequest request) {

        String email = jwtService.extraerUsuarioRefresh(
                request.refreshToken());

        UserDetails user =
                userDetailsService.loadUserByUsername(email);

        if (!jwtService.esRefreshValido(
                request.refreshToken(), user)) {
            throw new BadCredentialsException(
                    "Refresh token invalido");
        }

        return new AuthResponse(
                jwtService.generarAccessToken(user),
                request.refreshToken(),
                "Bearer",
                user.getUsername(),
                obtenerRoles(user)
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(
            @AuthenticationPrincipal UserDetails user) {

        return Map.of(
                "email", user.getUsername(),
                "roles", obtenerRoles(user)
        );
    }

    private AuthResponse crearRespuesta(UserDetails user) {
        return new AuthResponse(
                jwtService.generarAccessToken(user),
                jwtService.generarRefreshToken(user),
                "Bearer",
                user.getUsername(),
                obtenerRoles(user)
        );
    }

    private List<String> obtenerRoles(UserDetails user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
