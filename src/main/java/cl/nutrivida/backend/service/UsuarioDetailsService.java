package cl.nutrivida.backend.service;

import cl.nutrivida.backend.model.Usuario;
import cl.nutrivida.backend.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado"
            ));

        return User.withUsername(usuario.getEmail())
            .password(usuario.getPassword())
            .roles(usuario.getRol().name())
            .build();
    }
}
