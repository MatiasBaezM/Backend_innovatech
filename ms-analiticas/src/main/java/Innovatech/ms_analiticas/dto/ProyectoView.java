package Innovatech.ms_analiticas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Vista minima de un Proyecto recibido por HTTP desde ms-gestion_proyectos. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProyectoView {
    private String estado;
}
