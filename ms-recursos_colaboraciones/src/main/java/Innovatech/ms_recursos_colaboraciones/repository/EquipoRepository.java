package Innovatech.ms_recursos_colaboraciones.repository;

import Innovatech.ms_recursos_colaboraciones.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    Optional<Equipo> findByProyectoId(Long proyectoId);
}
