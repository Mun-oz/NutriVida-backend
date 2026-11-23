package cl.nutrivida.backend.config;

import cl.nutrivida.backend.model.Servicio;
import cl.nutrivida.backend.repository.ServicioRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "prod"})
public class ServiciosDevSeeder {

    @Bean
    public ApplicationRunner cargarServiciosIniciales(ServicioRepository servicios) {
        return args -> {
            agregarSiFalta(servicios, "NV001", "Evaluación Nutricional Inicial",
                    "Evaluación nutricional y orientación inicial.",
                    "60 minutos", 35000, "consulta_nutri.jpg");
            agregarSiFalta(servicios, "NV002", "Consulta Nutricional",
                    "Consulta personalizada de nutrición.",
                    "45 minutos", 30000, "clinNutri.jpg");
            agregarSiFalta(servicios, "NV003", "Control Nutricional",
                    "Seguimiento y ajuste del plan de alimentación.",
                    "30 minutos", 20000, "control_nutri_seg.jpg");
        };
    }

    private void agregarSiFalta(ServicioRepository servicios, String codigo,
            String nombre, String descripcion, String duracion,
            int precio, String imagen) {
        if (servicios.existsByCodigo(codigo)) {
            return;
        }
        Servicio servicio = new Servicio();
        servicio.setCodigo(codigo);
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setDuracion(duracion);
        servicio.setPrecio(precio);
        servicio.setImagen(imagen);
        servicios.save(servicio);
    }
}
