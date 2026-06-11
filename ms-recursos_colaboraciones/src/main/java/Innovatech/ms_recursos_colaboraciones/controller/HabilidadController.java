package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.Habilidad;
import Innovatech.ms_recursos_colaboraciones.service.HabilidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habilidades")
@RequiredArgsConstructor
public class HabilidadController {

    private final HabilidadService habilidadService;

    @GetMapping
    public ResponseEntity<List<Habilidad>> getAllHabilidades() {
        return ResponseEntity.ok(habilidadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habilidad> getHabilidadById(@PathVariable Long id) {
        return habilidadService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Habilidad> createHabilidad(@RequestBody Habilidad habilidad) {
        return ResponseEntity.ok(habilidadService.save(habilidad));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habilidad> updateHabilidad(@PathVariable Long id, @RequestBody Habilidad habilidad) {
        return habilidadService.findById(id)
                .map(existing -> {
                    habilidad.setId(existing.getId());
                    return ResponseEntity.ok(habilidadService.save(habilidad));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabilidad(@PathVariable Long id) {
        if (habilidadService.findById(id).isPresent()) {
            habilidadService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
