package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.Habilidad;
import Innovatech.ms_recursos_colaboraciones.repository.HabilidadRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabilidadService {

    private final HabilidadRepository habilidadRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Habilidad> findAll() {
        return habilidadRepository.findAll();
    }

    public Optional<Habilidad> findById(Long id) {
        return habilidadRepository.findById(id);
    }

    public Habilidad save(Habilidad habilidad) {
        return habilidadRepository.save(habilidad);
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createNativeQuery("DELETE FROM trabajador_habilidad WHERE habilidad_id = :id")
                .setParameter("id", id)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM db_autenticacion.usuario_habilidad WHERE habilidad_id = :id")
                .setParameter("id", id)
                .executeUpdate();
        habilidadRepository.deleteById(id);
    }
}
