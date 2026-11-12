package cl.nutrivida.backend.repository;

import cl.nutrivida.backend.model.Cita;
import cl.nutrivida.backend.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByUsuario_Id(Long usuarioId);

    List<Cita> findByServicio_Id(Long servicioId);

    List<Cita> findByEstado(EstadoCita estado);

    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    boolean existsByServicio_Id(Long servicioId);


    boolean existsByServicio_IdAndFechaAndHoraAndEstadoNot(
        Long servicioId,
        LocalDate fecha,
        LocalTime hora,
        EstadoCita estado
    );

    boolean existsByServicio_IdAndFechaAndHoraAndEstadoNotAndIdNot(
        Long servicioId,
        LocalDate fecha,
        LocalTime hora,
        EstadoCita estado,
        Long id
    );
}
