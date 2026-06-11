package Innovatech.ms_analiticas.service;

import Innovatech.ms_analiticas.dto.CargaTrabajoDTO;
import Innovatech.ms_analiticas.dto.CostoProyectoDTO;
import Innovatech.ms_analiticas.dto.GrupoConteoDTO;
import Innovatech.ms_analiticas.dto.ResumenDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnaliticasService {

    @PersistenceContext
    private EntityManager em;

    // Tablas en db_recursos: trabajadores, asignaciones_capacidad (DB activa del datasource)
    // Tablas en db_gestion_proyectos: proyectos, tareas (requieren nombre calificado)

    public ResumenDTO getResumen() {
        long totalProyectos    = count("SELECT COUNT(*) FROM db_gestion_proyectos.proyectos");
        long proyectosActivos  = count("SELECT COUNT(*) FROM db_gestion_proyectos.proyectos WHERE estado = 'EN_PROGRESO'");
        long totalTrabajadores = count("SELECT COUNT(*) FROM trabajadores");
        long tareasPendientes  = count("SELECT COUNT(*) FROM db_gestion_proyectos.tareas WHERE estado = 'POR_HACER'");
        long tareasCompletadas = count("SELECT COUNT(*) FROM db_gestion_proyectos.tareas WHERE estado = 'COMPLETADO'");
        double presupuesto = ((Number) em.createNativeQuery(
                "SELECT COALESCE(SUM(a.horas_asignadas * t.tarifa_hora), 0) " +
                "FROM asignaciones_capacidad a JOIN trabajadores t ON a.trabajador_id = t.id"
        ).getSingleResult()).doubleValue();

        return new ResumenDTO(totalProyectos, proyectosActivos, totalTrabajadores,
                tareasPendientes, tareasCompletadas, presupuesto);
    }

    @SuppressWarnings("unchecked")
    public List<GrupoConteoDTO> getProyectosPorEstado() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT estado, COUNT(*) FROM db_gestion_proyectos.proyectos GROUP BY estado"
        ).getResultList();
        return rows.stream()
                .map(r -> new GrupoConteoDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<GrupoConteoDTO> getTareasPorPrioridad() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT prioridad, COUNT(*) FROM db_gestion_proyectos.tareas GROUP BY prioridad"
        ).getResultList();
        return rows.stream()
                .map(r -> new GrupoConteoDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<GrupoConteoDTO> getTareasPorEstado() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT estado, COUNT(*) FROM db_gestion_proyectos.tareas GROUP BY estado"
        ).getResultList();
        return rows.stream()
                .map(r -> new GrupoConteoDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<CargaTrabajoDTO> getCargaTrabajo() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT t.nombre, SUM(a.horas_asignadas) as totalHoras " +
                "FROM trabajadores t " +
                "JOIN asignaciones_capacidad a ON t.id = a.trabajador_id " +
                "GROUP BY t.id, t.nombre " +
                "ORDER BY totalHoras DESC " +
                "LIMIT 5"
        ).getResultList();
        return rows.stream()
                .map(r -> new CargaTrabajoDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<CostoProyectoDTO> getCostosProyectos() {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT a.proyecto_id, SUM(a.horas_asignadas * t.tarifa_hora) as costoEstimado " +
                "FROM asignaciones_capacidad a " +
                "JOIN trabajadores t ON a.trabajador_id = t.id " +
                "GROUP BY a.proyecto_id " +
                "ORDER BY costoEstimado DESC"
        ).getResultList();
        return rows.stream()
                .map(r -> new CostoProyectoDTO(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()))
                .toList();
    }

    private long count(String sql) {
        return ((Number) em.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
