package Innovatech.ms_gestion_proyectos.dto;

import Innovatech.ms_gestion_proyectos.model.Actividad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta del feed del gestor: el historial de actividades de sus proyectos y,
 * destacadas aparte, las tareas que debe aprobar para completarlas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GestorActividadResponse {
    private List<Actividad> actividades;
    private List<TareaPorAprobar> porAprobar;
}
