package Innovatech.ms_autenticacion.repository;

import Innovatech.ms_autenticacion.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByRut(String rut);
    Optional<Usuario> findByRutAndClave(String rut, String clave);
    List<Usuario> findByRol(String rol);
}
