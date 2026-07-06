package Innovatech.ms_carta_gantt.dto;

import java.util.List;

/** Respuesta completa del endpoint GET /api/gantt/proyectos/{id}. */
public record GanttProyectoDTO(
        Long proyectoId,
        String nombreProyecto,
        String estado,
        String fechaInicioProyecto,
        String fechaFinProyecto,
        List<GanttTareaDTO> tareas
) {}
