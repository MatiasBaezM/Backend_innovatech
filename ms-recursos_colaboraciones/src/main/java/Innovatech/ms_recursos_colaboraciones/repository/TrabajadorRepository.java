package Innovatech.ms_recursos_colaboraciones.repository;

import Innovatech.ms_recursos_colaboraciones.model.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    Optional<Trabajador> findByRut(String rut);
    Optional<Trabajador> findByEmail(String email);
}
