package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.EquipoRequest;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.security.SecurityUtils;
import Innovatech.ms_gestion_proyectos.service.ProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;

    @GetMapping
    public List<Proyecto> getAllProyectos() {
        return proyectoService.getAllProyectos(SecurityUtils.extractRol(), SecurityUtils.extractUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> getProyectoById(@PathVariable Long id) {
        return proyectoService.getProyectoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Proyecto> createProyecto(@RequestBody Proyecto proyecto) {
        if ("COLABORADOR".equals(SecurityUtils.extractRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(proyectoService.createProyecto(proyecto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> updateProyecto(@PathVariable Long id, @RequestBody Proyecto proyecto) {
        try {
            return ResponseEntity.ok(proyectoService.updateProyecto(id, proyecto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/equipo")
    public ResponseEntity<Proyecto> updateEquipo(@PathVariable Long id, @RequestBody EquipoRequest request) {
        if (!"ADMINISTRADOR".equals(SecurityUtils.extractRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(proyectoService.updateEquipo(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProyecto(@PathVariable Long id) {
        if (!"ADMINISTRADOR".equals(SecurityUtils.extractRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        proyectoService.deleteProyecto(id);
        return ResponseEntity.noContent().build();
    }
}
