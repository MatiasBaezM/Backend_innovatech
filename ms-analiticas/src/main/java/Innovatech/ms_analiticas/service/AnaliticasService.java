package Innovatech.ms_analiticas.service;

import Innovatech.ms_analiticas.client.BackendClient;
import Innovatech.ms_analiticas.dto.AsignacionView;
import Innovatech.ms_analiticas.dto.CargaTrabajoDTO;
import Innovatech.ms_analiticas.dto.CostoProyectoDTO;
import Innovatech.ms_analiticas.dto.GrupoConteoDTO;
import Innovatech.ms_analiticas.dto.ProyectoView;
import Innovatech.ms_analiticas.dto.ResumenDTO;
import Innovatech.ms_analiticas.dto.TareaView;
import Innovatech.ms_analiticas.dto.TrabajadorView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Calcula las metricas del dashboard agregando en memoria los datos que obtiene
 * por HTTP de ms-gestion_proyectos y ms-recursos_colaboraciones. Reemplaza la
 * version anterior basada en SQL nativo cruzando schemas (incompatible con el
 * esquema de una RDS dedicada por microservicio).
 */
@Service
@RequiredArgsConstructor
public class AnaliticasService {

    private final BackendClient backend;

    public ResumenDTO getResumen(String auth) {
        List<ProyectoView> proyectos = backend.getProyectos(auth);
        List<TareaView> tareas = backend.getTareas(auth);
        List<TrabajadorView> trabajadores = backend.getTrabajadores(auth);
        List<AsignacionView> asignaciones = backend.getAsignaciones(auth);

        long totalProyectos = proyectos.size();
        long proyectosActivos = proyectos.stream()
                .filter(p -> "EN_PROGRESO".equals(p.getEstado())).count();
        long totalTrabajadores = trabajadores.size();
        long tareasPendientes = tareas.stream()
                .filter(t -> "POR_HACER".equals(t.getEstado())).count();
        long tareasCompletadas = tareas.stream()
                .filter(t -> "COMPLETADO".equals(t.getEstado())).count();
        double presupuesto = asignaciones.stream()
                .filter(this::tieneCosto)
                .mapToDouble(this::costo)
                .sum();

        return new ResumenDTO(totalProyectos, proyectosActivos, totalTrabajadores,
                tareasPendientes, tareasCompletadas, presupuesto);
    }

    public List<GrupoConteoDTO> getProyectosPorEstado(String auth) {
        return contar(backend.getProyectos(auth).stream().map(ProyectoView::getEstado));
    }

    public List<GrupoConteoDTO> getTareasPorPrioridad(String auth) {
        return contar(backend.getTareas(auth).stream().map(TareaView::getPrioridad));
    }

    public List<GrupoConteoDTO> getTareasPorEstado(String auth) {
        return contar(backend.getTareas(auth).stream().map(TareaView::getEstado));
    }

    public List<CargaTrabajoDTO> getCargaTrabajo(String auth) {
        Map<String, Long> horasPorTrabajador = backend.getAsignaciones(auth).stream()
                .filter(a -> a.getTrabajador() != null && a.getTrabajador().getNombre() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTrabajador().getNombre(),
                        Collectors.summingLong(a -> a.getHorasAsignadas() == null ? 0L : a.getHorasAsignadas())));
        return horasPorTrabajador.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new CargaTrabajoDTO(e.getKey(), e.getValue()))
                .toList();
    }

    public List<CostoProyectoDTO> getCostosProyectos(String auth) {
        Map<Long, Double> costoPorProyecto = backend.getAsignaciones(auth).stream()
                .filter(a -> a.getProyectoId() != null && tieneCosto(a))
                .collect(Collectors.groupingBy(
                        AsignacionView::getProyectoId,
                        Collectors.summingDouble(this::costo)));
        return costoPorProyecto.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(e -> new CostoProyectoDTO(e.getKey(), e.getValue()))
                .toList();
    }

    // Agrupa una secuencia de etiquetas (ignorando nulls) en conteos label -> cantidad.
    private List<GrupoConteoDTO> contar(Stream<String> labels) {
        Map<String, Long> conteo = labels.filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return conteo.entrySet().stream()
                .map(e -> new GrupoConteoDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private boolean tieneCosto(AsignacionView a) {
        return a.getTrabajador() != null
                && a.getTrabajador().getTarifaHora() != null
                && a.getHorasAsignadas() != null;
    }

    private double costo(AsignacionView a) {
        return a.getHorasAsignadas() * a.getTrabajador().getTarifaHora();
    }
}
