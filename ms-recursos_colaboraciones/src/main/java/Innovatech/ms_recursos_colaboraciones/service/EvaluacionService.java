package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.Evaluacion;
import Innovatech.ms_recursos_colaboraciones.repository.EvaluacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;

    public List<Evaluacion> findAll() {
        return evaluacionRepository.findAll();
    }

    public Optional<Evaluacion> findById(Long id) {
        return evaluacionRepository.findById(id);
    }

    public Evaluacion save(Evaluacion evaluacion) {
        return evaluacionRepository.save(evaluacion);
    }

    public void deleteById(Long id) {
        evaluacionRepository.deleteById(id);
    }
}
