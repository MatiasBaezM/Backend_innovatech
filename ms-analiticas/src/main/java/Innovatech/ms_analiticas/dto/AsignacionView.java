package Innovatech.ms_analiticas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Vista minima de una AsignacionCapacidad recibida por HTTP desde ms-recursos_colaboraciones. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsignacionView {
    private TrabajadorView trabajador;
    private Long proyectoId;
    private Integer horasAsignadas;
}
