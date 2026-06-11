package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.Trabajador;
import Innovatech.ms_recursos_colaboraciones.service.TrabajadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trabajadores")
@RequiredArgsConstructor
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    @GetMapping
    public ResponseEntity<List<Trabajador>> getAllTrabajadores() {
        return ResponseEntity.ok(trabajadorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trabajador> getTrabajadorById(@PathVariable Long id) {
        return trabajadorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rut/{rut}")
    public ResponseEntity<Trabajador> getTrabajadorByRut(@PathVariable String rut) {
        return trabajadorService.findByRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Trabajador> createTrabajador(@RequestBody Trabajador trabajador) {
        return ResponseEntity.ok(trabajadorService.save(trabajador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trabajador> updateTrabajador(@PathVariable Long id, @RequestBody Trabajador trabajador) {
        return trabajadorService.findById(id)
                .map(existing -> {
                    trabajador.setId(existing.getId());
                    return ResponseEntity.ok(trabajadorService.save(trabajador));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrabajador(@PathVariable Long id) {
        if (trabajadorService.findById(id).isPresent()) {
            trabajadorService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
