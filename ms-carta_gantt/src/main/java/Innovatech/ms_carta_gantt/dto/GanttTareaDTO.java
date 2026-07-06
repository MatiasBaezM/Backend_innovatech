package Innovatech.ms_carta_gantt.dto;

/**
 * Tarea formateada para la carta Gantt.
 * El frontend usa fechaInicio/fechaFin para calcular el offset y ancho de la barra.
 */
public record GanttTareaDTO(
        Long id,
        String titulo,
        String estado,
        String prioridad,
        String asignadoNombre,
        String fechaInicio, // = Tarea.fechaCreacion en ms-gestion_proyectos
        String fechaFin,    // = Tarea.fechaLimite en ms-gestion_proyectos
        Integer horasEstimadas,
        Integer orden        // posicion calculada por orden de fechaInicio
) {}
