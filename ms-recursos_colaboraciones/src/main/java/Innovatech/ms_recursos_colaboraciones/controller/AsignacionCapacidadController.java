package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.AsignacionCapacidad;
import Innovatech.ms_recursos_colaboraciones.service.AsignacionCapacidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
public class AsignacionCapacidadController {

    private final AsignacionCapacidadService asignacionService;

    @GetMapping
    public ResponseEntity<List<AsignacionCapacidad>> getAllAsignaciones() {
        return ResponseEntity.ok(asignacionService.findAll());
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public ResponseEntity<List<AsignacionCapacidad>> getAsignacionesByTrabajador(@PathVariable Long trabajadorId) {
        return ResponseEntity.ok(asignacionService.findByTrabajadorId(trabajadorId));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public ResponseEntity<List<AsignacionCapacidad>> getAsignacionesByProyecto(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(asignacionService.findByProyectoId(proyectoId));
    }

    @PostMapping
    public ResponseEntity<AsignacionCapacidad> createAsignacion(@RequestBody AsignacionCapacidad asignacion) {
        return ResponseEntity.ok(asignacionService.save(asignacion));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<AsignacionCapacidad>> createAsignacionesBatch(@RequestBody List<AsignacionCapacidad> asignaciones) {
        return ResponseEntity.ok(asignacionService.saveAll(asignaciones));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsignacionCapacidad> updateAsignacion(@PathVariable Long id, @RequestBody AsignacionCapacidad asignacion) {
        return asignacionService.findById(id)
                .map(existing -> {
                    asignacion.setId(existing.getId());
                    return ResponseEntity.ok(asignacionService.save(asignacion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsignacion(@PathVariable Long id) {
        if (asignacionService.findById(id).isPresent()) {
            asignacionService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
