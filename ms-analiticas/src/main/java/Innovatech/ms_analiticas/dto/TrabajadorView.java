package Innovatech.ms_analiticas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Vista minima de un Trabajador recibido por HTTP desde ms-recursos_colaboraciones. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrabajadorView {
    private Long id;
    private String nombre;
    private Double tarifaHora;
}
