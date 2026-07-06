package Innovatech.ms_carta_gantt.service;

import Innovatech.ms_carta_gantt.client.BackendClient;
import Innovatech.ms_carta_gantt.dto.GanttProyectoDTO;
import Innovatech.ms_carta_gantt.dto.GanttTareaDTO;
import Innovatech.ms_carta_gantt.dto.ProyectoExternoDTO;
import Innovatech.ms_carta_gantt.dto.TareaExternaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Combina proyecto y tareas obtenidos de ms-gestion_proyectos y arma la
 * estructura que el frontend usa para dibujar la carta Gantt.
 */
@Service
@RequiredArgsConstructor
public class GanttService {

    private final BackendClient backendClient;

    public GanttProyectoDTO getGanttProyecto(Long proyectoId, String auth) {
        ProyectoExternoDTO proyecto = fetchProyecto(proyectoId, auth);
        List<TareaExternaDTO> tareas = fetchTareas(proyectoId, auth);

        List<TareaExternaDTO> tareasConFechas = tareas.stream()
                .filter(t -> t.getFechaCreacion() != null && !t.getFechaCreacion().isBlank()
                        && t.getFechaLimite() != null && !t.getFechaLimite().isBlank())
                .sorted(Comparator.comparing(TareaExternaDTO::getFechaCreacion))
                .toList();

        AtomicInteger orden = new AtomicInteger(1);
        List<GanttTareaDTO> tareasGantt = tareasConFechas.stream()
                .map(t -> new GanttTareaDTO(
                        t.getId(),
                        t.getTitulo(),
                        t.getEstado(),
                        t.getPrioridad(),
                        t.getAsignadoNombre() != null ? t.getAsignadoNombre() : "Sin asignar",
                        t.getFechaCreacion(),
                        t.getFechaLimite(),
                        t.getHorasEstimadas(),
                        orden.getAndIncrement()
                ))
                .toList();

        return new GanttProyectoDTO(
                proyectoId,
                proyecto != null ? proyecto.getNombre() : "Proyecto " + proyectoId,
                proyecto != null ? proyecto.getEstado() : "EN_PROGRESO",
                proyecto != null ? proyecto.getFechaInicio() : null,
                proyecto != null ? proyecto.getFechaFin() : null,
                tareasGantt
        );
    }

    private ProyectoExternoDTO fetchProyecto(Long proyectoId, String auth) {
        try {
            return backendClient.getProyecto(proyectoId, auth);
        } catch (RestClientException e) {
            return null;
        }
    }

    private List<TareaExternaDTO> fetchTareas(Long proyectoId, String auth) {
        try {
            List<TareaExternaDTO> result = backendClient.getTareas(proyectoId, auth);
            return result != null ? result : Collections.emptyList();
        } catch (RestClientException e) {
            return Collections.emptyList();
        }
    }
}
