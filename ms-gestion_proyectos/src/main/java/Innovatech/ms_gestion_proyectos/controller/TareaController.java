package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.RechazoRequest;
import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.security.SecurityUtils;
import Innovatech.ms_gestion_proyectos.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos/{proyectoId}/tareas")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @GetMapping
    public ResponseEntity<List<Tarea>> getTareas(@PathVariable @NonNull Long proyectoId) {
        return ResponseEntity.ok(tareaService.getTareasByProyecto(proyectoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> getTareaById(@PathVariable Long proyectoId, @PathVariable @NonNull Long id) {
        return tareaService.getTareaById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tarea> createTarea(@PathVariable @NonNull Long proyectoId, @RequestBody Tarea tarea) {
        String rol = SecurityUtils.extractRol();
        if ("COLABORADOR".equals(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(tareaService.createTarea(proyectoId, tarea, rol, SecurityUtils.extractUserId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarea> updateTarea(@PathVariable Long proyectoId, @PathVariable @NonNull Long id,
                                              @RequestBody Tarea tarea) {
        if (tarea.getEstado() == Tarea.Estado.REVISADO && "COLABORADOR".equals(SecurityUtils.extractRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(tareaService.updateTarea(id, tarea));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<Tarea> aprobarTarea(@PathVariable Long proyectoId, @PathVariable @NonNull Long id) {
        if (!"ADMINISTRADOR".equals(SecurityUtils.extractRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(tareaService.aprobarTarea(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<Tarea> rechazarTarea(@PathVariable Long proyectoId, @PathVariable @NonNull Long id,
                                                @RequestBody RechazoRequest request) {
        if (!"ADMINISTRADOR".equals(SecurityUtils.extractRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(tareaService.rechazarTarea(id, request.getMensajeCorreccion()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarea(@PathVariable Long proyectoId, @PathVariable @NonNull Long id) {
        tareaService.deleteTarea(id);
        return ResponseEntity.noContent().build();
    }
}
