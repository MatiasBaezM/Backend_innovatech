package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.dto.GestorActividadResponse;
import Innovatech.ms_gestion_proyectos.model.Actividad;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
import Innovatech.ms_gestion_proyectos.security.SecurityUtils;
import Innovatech.ms_gestion_proyectos.service.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadRepository actividadRepository;
    private final ActividadService actividadService;

    @GetMapping
    public ResponseEntity<List<Actividad>> getRecentActivities() {
        return ResponseEntity.ok(actividadRepository.findTop10ByOrderByFechaCreacionDesc());
    }

    /**
     * Feed del gestor: actividades de sus proyectos y tareas que debe aprobar.
     * El rol y el id se toman del JWT validado por el filtro de seguridad.
     */
    @GetMapping("/gestor")
    public ResponseEntity<GestorActividadResponse> getFeedGestor() {
        return ResponseEntity.ok(
                actividadService.getFeedGestor(SecurityUtils.extractRol(), SecurityUtils.extractUserId()));
    }

    @PostMapping
    public ResponseEntity<Actividad> createActividad(@RequestBody Actividad actividad) {
        return ResponseEntity.ok(actividadRepository.save(actividad));
    }
}
