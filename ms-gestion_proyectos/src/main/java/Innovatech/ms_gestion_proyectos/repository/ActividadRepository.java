package Innovatech.ms_gestion_proyectos.repository;

import Innovatech.ms_gestion_proyectos.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findTop10ByOrderByFechaCreacionDesc();
}
