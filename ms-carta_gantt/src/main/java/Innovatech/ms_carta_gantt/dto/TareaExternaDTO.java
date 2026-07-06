package Innovatech.ms_carta_gantt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Mapea la respuesta de GET /api/proyectos/{id}/tareas en ms-gestion_proyectos.
 * Solo se leen los campos que la carta Gantt necesita; el resto se ignora.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TareaExternaDTO {
    private Long id;
    private Long proyectoId;
    private String titulo;
    private String estado;       // POR_HACER | EN_PROGRESO | COMPLETADO | REVISADO
    private String prioridad;    // ALTA | MEDIA | BAJA
    private String asignadoNombre;
    private String fechaCreacion; // LocalDate ISO "YYYY-MM-DD" -> usada como inicio en el Gantt
    private String fechaLimite;   // LocalDate ISO "YYYY-MM-DD" -> usada como fin en el Gantt
    private Integer horasEstimadas;
}
