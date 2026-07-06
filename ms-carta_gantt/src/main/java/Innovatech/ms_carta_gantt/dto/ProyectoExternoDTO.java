package Innovatech.ms_carta_gantt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Mapea la respuesta de GET /api/proyectos/{id} en ms-gestion_proyectos. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProyectoExternoDTO {
    private Long id;
    private String nombre;
    private String estado;
    private String fechaInicio; // LocalDate ISO -- inicio del proyecto (eje X del Gantt)
    private String fechaFin;    // Puede no existir en ms-gestion_proyectos; queda null en ese caso
}
