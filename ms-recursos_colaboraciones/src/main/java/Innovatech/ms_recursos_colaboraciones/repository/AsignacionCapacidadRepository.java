package Innovatech.ms_recursos_colaboraciones.repository;

import Innovatech.ms_recursos_colaboraciones.model.AsignacionCapacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionCapacidadRepository extends JpaRepository<AsignacionCapacidad, Long> {
    List<AsignacionCapacidad> findByTrabajadorId(Long trabajadorId);
    List<AsignacionCapacidad> findByProyectoId(Long proyectoId);
}
