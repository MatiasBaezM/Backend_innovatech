package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.Equipo;
import Innovatech.ms_recursos_colaboraciones.repository.EquipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;

    public List<Equipo> findAll() {
        return equipoRepository.findAll();
    }

    public Optional<Equipo> findById(Long id) {
        return equipoRepository.findById(id);
    }

    public Optional<Equipo> findByProyectoId(Long proyectoId) {
        return equipoRepository.findByProyectoId(proyectoId);
    }

    public Equipo save(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    public void deleteById(Long id) {
        equipoRepository.deleteById(id);
    }
}
