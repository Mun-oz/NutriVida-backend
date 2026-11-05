package cl.nutrivida.backend.config;

import cl.nutrivida.backend.model.Rol;
import cl.nutrivida.backend.model.Usuario;
import cl.nutrivida.backend.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
@Profile("dev")
public class AdminDevSeeder {

    @Bean
    public ApplicationRunner crearAdminDesarrollo(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            @Value("${bootstrap.admin.email:}") String email,
            @Value("${bootstrap.admin.password:}") String password) {

        return args -> {

            if (email.isBlank() || password.isBlank()) {
                return;
            }

            if (password.length() < 12) {
                throw new IllegalStateException(
                    "La clave del administrador debe tener al menos 12 caracteres"
                );
            }

            String emailNormalizado =
                    email.trim().toLowerCase(Locale.ROOT);

            if (usuarios.existsByEmail(emailNormalizado)) {
                return;
            }

            Usuario admin = new Usuario();

            admin.setNombre("Administrador NutriVida");
            admin.setEmail(emailNormalizado);
            admin.setPassword(encoder.encode(password));
            admin.setRol(Rol.ADMIN);

            usuarios.save(admin);
        };
    }
}
