package Innovatech.ms_recursos_colaboraciones.repository;

import Innovatech.ms_recursos_colaboraciones.model.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabilidadRepository extends JpaRepository<Habilidad, Long> {
}
