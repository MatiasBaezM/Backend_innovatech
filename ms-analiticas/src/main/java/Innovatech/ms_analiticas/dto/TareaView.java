package Innovatech.ms_analiticas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Vista minima de una Tarea recibida por HTTP desde ms-gestion_proyectos. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TareaView {
    private String estado;
    private String prioridad;
}
