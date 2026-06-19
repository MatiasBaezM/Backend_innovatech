package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.dto.GestorActividadResponse;
import Innovatech.ms_gestion_proyectos.dto.TareaPorAprobar;
import Innovatech.ms_gestion_proyectos.model.Actividad;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
import Innovatech.ms_gestion_proyectos.repository.ProyectoRepository;
import Innovatech.ms_gestion_proyectos.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ProyectoRepository proyectoRepository;
    private final TareaRepository tareaRepository;

    /**
     * Feed para el gestor: las actividades de los proyectos que gestiona y, aparte,
     * las tareas COMPLETADO que debe aprobar. Un ADMINISTRADOR ve todos los proyectos.
     */
    public GestorActividadResponse getFeedGestor(String rol, Long userId) {
        List<Proyecto> proyectos = proyectoRepository.findAll();

        List<Proyecto> visibles = "GESTOR_PROYECTOS".equals(rol) && userId != null
                ? proyectos.stream().filter(p -> userId.equals(p.getGestorId())).toList()
                : proyectos;

        List<Long> ids = visibles.stream().map(Proyecto::getId).toList();
        if (ids.isEmpty()) {
            return new GestorActividadResponse(List.of(), List.of());
        }

        List<Actividad> actividades = actividadRepository.findTop30ByProyectoIdInOrderByFechaCreacionDesc(ids);

        Map<Long, String> nombrePorProyecto = visibles.stream()
                .collect(Collectors.toMap(Proyecto::getId, p -> p.getNombre() != null ? p.getNombre() : ""));

        List<TareaPorAprobar> porAprobar = tareaRepository
                .findByProyectoIdInAndEstado(ids, Tarea.Estado.COMPLETADO).stream()
                .map(t -> new TareaPorAprobar(
                        t.getId(),
                        t.getProyectoId(),
                        nombrePorProyecto.get(t.getProyectoId()),
                        t.getTitulo(),
                        t.getAsignadoNombre()))
                .toList();

        return new GestorActividadResponse(actividades, porAprobar);
    }
}
