package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.Trabajador;
import Innovatech.ms_recursos_colaboraciones.repository.TrabajadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;

    public List<Trabajador> findAll() {
        return trabajadorRepository.findAll();
    }

    public Optional<Trabajador> findById(Long id) {
        return trabajadorRepository.findById(id);
    }

    public Optional<Trabajador> findByRut(String rut) {
        return trabajadorRepository.findByRut(rut);
    }

    public Optional<Trabajador> findByEmail(String email) {
        return trabajadorRepository.findByEmail(email);
    }

    public Trabajador save(Trabajador trabajador) {
        return trabajadorRepository.save(trabajador);
    }

    public void deleteById(Long id) {
        trabajadorRepository.deleteById(id);
    }
}
