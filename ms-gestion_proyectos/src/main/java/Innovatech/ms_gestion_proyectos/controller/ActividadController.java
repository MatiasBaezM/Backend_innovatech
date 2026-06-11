package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.Actividad;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos/actividades")
public class ActividadController {

    @Autowired
    private ActividadRepository actividadRepository;

    @GetMapping
    public ResponseEntity<List<Actividad>> getRecentActivities() {
        return ResponseEntity.ok(actividadRepository.findTop10ByOrderByFechaCreacionDesc());
    }

    @PostMapping
    public ResponseEntity<Actividad> createActividad(@RequestBody Actividad actividad) {
        return ResponseEntity.ok(actividadRepository.save(actividad));
    }
}
