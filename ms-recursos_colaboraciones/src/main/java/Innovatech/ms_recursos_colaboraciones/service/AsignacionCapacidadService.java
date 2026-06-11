package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.AsignacionCapacidad;
import Innovatech.ms_recursos_colaboraciones.repository.AsignacionCapacidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsignacionCapacidadService {

    private final AsignacionCapacidadRepository asignacionRepository;

    public List<AsignacionCapacidad> findAll() {
        return asignacionRepository.findAll();
    }

    public List<AsignacionCapacidad> findByTrabajadorId(Long trabajadorId) {
        return asignacionRepository.findByTrabajadorId(trabajadorId);
    }

    public List<AsignacionCapacidad> findByProyectoId(Long proyectoId) {
        return asignacionRepository.findByProyectoId(proyectoId);
    }

    public Optional<AsignacionCapacidad> findById(Long id) {
        return asignacionRepository.findById(id);
    }

    public AsignacionCapacidad save(AsignacionCapacidad asignacion) {
        return asignacionRepository.save(asignacion);
    }

    public List<AsignacionCapacidad> saveAll(List<AsignacionCapacidad> asignaciones) {
        return asignacionRepository.saveAll(asignaciones);
    }

    public void deleteById(Long id) {
        asignacionRepository.deleteById(id);
    }
}
