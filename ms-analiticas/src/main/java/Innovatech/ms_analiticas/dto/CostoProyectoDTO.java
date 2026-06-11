package Innovatech.ms_analiticas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoProyectoDTO {
    private Long proyectoId;
    private double costoEstimado;
}
