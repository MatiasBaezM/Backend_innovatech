package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.model.Tarea;
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

    public List<Tarea> getTareasByProyecto(@NonNull Long proyectoId) {
        return tareaRepository.findByProyectoId(proyectoId);
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
        return tareaRepository.save(tarea);
    }

    public Tarea updateTarea(Long id, Tarea tareaDetails) {
        return tareaRepository.findById(id).map(tarea -> {
            tarea.setTitulo(tareaDetails.getTitulo());
            tarea.setDescripcion(tareaDetails.getDescripcion());
            tarea.setFechaLimite(tareaDetails.getFechaLimite());
            tarea.setAsignadoId(tareaDetails.getAsignadoId());
            tarea.setAsignadoNombre(tareaDetails.getAsignadoNombre());
            tarea.setPrioridad(tareaDetails.getPrioridad());
            tarea.setEstado(tareaDetails.getEstado());
            tarea.setMensajeCorreccion(tareaDetails.getMensajeCorreccion());
            return tareaRepository.save(tarea);
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public Tarea aprobarTarea(Long id) {
        return tareaRepository.findById(id).map(tarea -> {
            tarea.setEstado(Tarea.Estado.REVISADO);
            tarea.setMensajeCorreccion(null);
            return tareaRepository.save(tarea);
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public Tarea rechazarTarea(Long id, String mensajeCorreccion) {
        return tareaRepository.findById(id).map(tarea -> {
            tarea.setEstado(Tarea.Estado.POR_HACER);
            tarea.setMensajeCorreccion(mensajeCorreccion);
            return tareaRepository.save(tarea);
        }).orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    public void deleteTarea(@NonNull Long id) {
        tareaRepository.deleteById(id);
    }
}
