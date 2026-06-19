package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Listado global de tareas (todas, sin filtrar por proyecto). Lo consume
 * ms-analiticas para agregar conteos por estado/prioridad via HTTP, en vez de
 * leer la tabla directamente por SQL.
 *
 * La ruta literal /api/proyectos/tareas tiene precedencia sobre /api/proyectos/{id}
 * de ProyectoController (igual que /api/proyectos/actividades de ActividadController).
 */
@RestController
@RequestMapping("/api/proyectos/tareas")
@RequiredArgsConstructor
public class TareaGlobalController {

    private final TareaService tareaService;

    @GetMapping
    public List<Tarea> getAllTareas() {
        return tareaService.getAllTareas();
    }
}
