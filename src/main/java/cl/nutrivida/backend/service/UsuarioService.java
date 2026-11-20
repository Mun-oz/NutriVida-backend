
package cl.nutrivida.backend.service;

import cl.nutrivida.backend.dto.*;
import cl.nutrivida.backend.model.*;
import cl.nutrivida.backend.repository.*;
import cl.nutrivida.backend.validation.RunUtils;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final CitaRepository citas;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarios,
            CitaRepository citas,
            PasswordEncoder passwordEncoder) {

        this.usuarios = usuarios;
        this.citas = citas;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return convertir(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {

        String email = normalizarEmail(request.email());
        String run = normalizarRun(request.run());

        verificarDuplicados(email, run, null);

        Usuario usuario = new Usuario();

        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setRun(run);
        usuario.setRol(request.rol());
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());

        return convertir(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(
            Long id,
            UsuarioActualizarRequest request,
            String administradorEmail) {

        Usuario usuario = buscar(id);

        String email = normalizarEmail(request.email());
        String run = normalizarRun(request.run());

        // Evitar que el administrador se quite su propio
        // rol o cambie su correo mientras tiene sesion.
        if (usuario.getEmail().equalsIgnoreCase(administradorEmail)
                && (request.rol() != Rol.ADMIN
                    || !usuario.getEmail().equals(email))) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes quitarte el rol ADMIN ni cambiar tu correo"
            );
        }

        verificarDuplicados(email, run, id);

        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setRun(run);
        usuario.setRol(request.rol());
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setDireccion(request.direccion());

        if (request.password() != null) {
            usuario.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        return convertir(usuarios.save(usuario));
    }

    @Transactional
    public void eliminar(Long id, String administradorEmail) {

        Usuario usuario = buscar(id);

        if (usuario.getEmail().equalsIgnoreCase(administradorEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes eliminar tu propia cuenta"
            );
        }

        if (citas.existsByUsuario_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario tiene citas asociadas"
            );
        }

        usuarios.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    private void verificarDuplicados(
            String email,
            String run,
            Long idActual) {

        usuarios.findByEmail(email).ifPresent(usuario -> {
            if (!usuario.getId().equals(idActual)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El correo ya esta registrado"
                );
            }
        });

        if (run != null) {
            usuarios.findByRun(run).ifPresent(usuario -> {
                if (!usuario.getId().equals(idActual)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "El RUN ya esta registrado"
                    );
                }
            });
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarRun(String run) {
        return RunUtils.normalizar(run);
    }

    private UsuarioResponse convertir(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRun(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getDireccion()
        );
    }
}
