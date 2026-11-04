package cl.nutrivida.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/api/v1/servicios",
                    "/api/v1/servicios/**"
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/servicios",
                        "/api/v1/servicios/**"
                ).permitAll()

                .requestMatchers(
                        "/api/v1/servicios",
                        "/api/v1/servicios/**"
                ).hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
