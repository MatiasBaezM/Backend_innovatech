package Innovatech.ms_analiticas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDTO {
    private long totalProyectos;
    private long proyectosActivos;
    private long totalTrabajadores;
    private long tareasPendientes;
    private long tareasCompletadas;
    private double presupuestoTotal;
}
