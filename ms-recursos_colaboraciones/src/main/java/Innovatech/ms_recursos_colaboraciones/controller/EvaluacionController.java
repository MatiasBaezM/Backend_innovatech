package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.Evaluacion;
import Innovatech.ms_recursos_colaboraciones.service.EvaluacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
@RequiredArgsConstructor
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    @GetMapping
    public ResponseEntity<List<Evaluacion>> getAllEvaluaciones() {
        return ResponseEntity.ok(evaluacionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evaluacion> getEvaluacionById(@PathVariable Long id) {
        return evaluacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Evaluacion> createEvaluacion(@RequestBody Evaluacion evaluacion) {
        return ResponseEntity.ok(evaluacionService.save(evaluacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evaluacion> updateEvaluacion(@PathVariable Long id, @RequestBody Evaluacion evaluacion) {
        return evaluacionService.findById(id)
                .map(existing -> {
                    evaluacion.setId(existing.getId());
                    return ResponseEntity.ok(evaluacionService.save(evaluacion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvaluacion(@PathVariable Long id) {
        if (evaluacionService.findById(id).isPresent()) {
            evaluacionService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
