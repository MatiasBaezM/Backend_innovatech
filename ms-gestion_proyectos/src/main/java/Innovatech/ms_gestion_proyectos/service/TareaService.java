package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.model.Actividad;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
import Innovatech.ms_gestion_proyectos.repository.ProyectoRepository;
import Innovatech.ms_gestion_proyectos.repository.TareaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TareaService {

    private final TareaRepository tareaRepository;
    private final ProyectoRepository proyectoRepository;
    private final ActividadRepository actividadRepository;

    public List<Tarea> getTareasByProyecto(@NonNull Long proyectoId) {
        return tareaRepository.findByProyectoId(proyectoId);
    }

    public List<Tarea> getAllTareas() {
        return tareaRepository.findAll();
    }

    public Optional<Tarea> getTareaById(@NonNull Long id) {
        return tareaRepository.findById(id);
    }

    public Tarea createTarea(@NonNull Long proyectoId, Tarea tarea, String rol, Long userId) {
        if ("GESTOR_PROYECTOS".equals(rol)) {
            Proyecto proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
            if (proyecto.getGestorId() == null || !proyecto.getGestorId().equals(userId)) {
                throw new SecurityException("No autorizado para crear tareas en este proyecto");
            }
        }
        tarea.setProyectoId(proyectoId);
        Tarea guardada = tareaRepository.save(tarea);
        registrarActividad(guardada, "ASIGNADA",
                "Tarea \"" + guardada.getTitulo() + "\" asignada a " + nombreTrabajador(guardada));
        return guardada;
    }

    public Tarea updateTarea(Long id, Tarea tareaDetails) {
        return tareaRepository.findById(id).map(tarea -> {
            Tarea.Estado estadoAnterior = tarea.getEstado();
            tarea.setTitulo(tareaDetails.getTitulo());
            tarea.setDescripcion(tareaDetails.getDescripcion());
            tarea.setFechaLimite(tareaDetails.getFechaLimite());
            tarea.setAsignadoId(tareaDetails.getAsignadoId());
            tarea.setAsignadoNombre(tareaDetails.getAsignadoNombre());
            tarea.setPrioridad(tareaDetails.getPrioridad());
            tarea.setEstado(tareaDetails.getEstado());
            tarea.setMensajeCorreccion(tareaDetails.getMensajeCorreccion());
            Tarea guardada = tareaRepository.save(tarea);
            if (guardada.getEstado() != null && guardada.getEstado() != estadoAnterior) {
                registrarTransicion(guardada);
            }
            return guardada;
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public Tarea aprobarTarea(Long id) {
        return tareaRepository.findById(id).map(tarea -> {
            tarea.setEstado(Tarea.Estado.REVISADO);
            tarea.setMensajeCorreccion(null);
            Tarea guardada = tareaRepository.save(tarea);
            registrarTransicion(guardada);
            return guardada;
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public Tarea rechazarTarea(Long id, String mensajeCorreccion) {
        return tareaRepository.findById(id).map(tarea -> {
            tarea.setEstado(Tarea.Estado.POR_HACER);
            tarea.setMensajeCorreccion(mensajeCorreccion);
            Tarea guardada = tareaRepository.save(tarea);
            registrarTransicion(guardada);
            return guardada;
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public void deleteTarea(@NonNull Long id) {
        tareaRepository.deleteById(id);
    }

    // ── Registro de actividad ──────────────────────────────────────────────────

    /** Registra la actividad correspondiente al nuevo estado de la tarea. */
    private void registrarTransicion(Tarea tarea) {
        if (tarea.getEstado() == null) return;
        String nombre = nombreTrabajador(tarea);
        switch (tarea.getEstado()) {
            case EN_PROGRESO -> registrarActividad(tarea, "EN_PROGRESO",
                    nombre + " comenzó la tarea \"" + tarea.getTitulo() + "\"");
            case COMPLETADO -> registrarActividad(tarea, "COMPLETADO",
                    nombre + " completó la tarea \"" + tarea.getTitulo() + "\" (pendiente de aprobación)");
            case REVISADO -> registrarActividad(tarea, "REVISADO",
                    "Tarea \"" + tarea.getTitulo() + "\" aprobada");
            case POR_HACER -> {
                if (tarea.getMensajeCorreccion() != null && !tarea.getMensajeCorreccion().isBlank()) {
                    registrarActividad(tarea, "RECHAZADO",
                            "Tarea \"" + tarea.getTitulo() + "\" devuelta para correcciones");
                }
            }
        }
    }

    private void registrarActividad(Tarea tarea, String tipo, String titulo) {
        String proyectoNombre = tarea.getProyectoId() == null ? null
                : proyectoRepository.findById(tarea.getProyectoId())
                        .map(Proyecto::getNombre).orElse(null);
        Actividad actividad = Actividad.builder()
                .titulo(titulo)
                .trabajadorId(tarea.getAsignadoId())
                .trabajadorNombre(tarea.getAsignadoNombre())
                .proyectoId(tarea.getProyectoId())
                .proyectoNombre(proyectoNombre)
                .tareaId(tarea.getId())
                .tipo(tipo)
                .build();
        actividadRepository.save(actividad);
    }

    private String nombreTrabajador(Tarea tarea) {
        return tarea.getAsignadoNombre() != null && !tarea.getAsignadoNombre().isBlank()
                ? tarea.getAsignadoNombre() : "Un colaborador";
    }
}
