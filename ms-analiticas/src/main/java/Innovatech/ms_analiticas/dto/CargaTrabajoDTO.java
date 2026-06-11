package Innovatech.ms_analiticas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargaTrabajoDTO {
    private String nombre;
    private long totalHoras;
}
