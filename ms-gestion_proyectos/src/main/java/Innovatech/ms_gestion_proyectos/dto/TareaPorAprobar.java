package Innovatech.ms_gestion_proyectos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tarea COMPLETADA esperando la aprobación del gestor (item destacado del panel). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaPorAprobar {
    private Long id;
    private Long proyectoId;
    private String proyectoNombre;
    private String titulo;
    private String asignadoNombre;
}
