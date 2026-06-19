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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnaliticasServiceTest {

    private static final String AUTH = "Bearer test-token";

    @Mock private BackendClient backend;
    @InjectMocks private AnaliticasService analiticasService;

    // ── helpers de construccion ───────────────────────────────────────────────

    private ProyectoView proyecto(String estado) {
        ProyectoView p = new ProyectoView();
        p.setEstado(estado);
        return p;
    }

    private TareaView tarea(String estado, String prioridad) {
        TareaView t = new TareaView();
        t.setEstado(estado);
        t.setPrioridad(prioridad);
        return t;
    }

    private TrabajadorView trabajador(Long id, String nombre, Double tarifa) {
        TrabajadorView w = new TrabajadorView();
        w.setId(id);
        w.setNombre(nombre);
        w.setTarifaHora(tarifa);
        return w;
    }

    private AsignacionView asignacion(TrabajadorView t, Long proyectoId, Integer horas) {
        AsignacionView a = new AsignacionView();
        a.setTrabajador(t);
        a.setProyectoId(proyectoId);
        a.setHorasAsignadas(horas);
        return a;
    }

    // ── getResumen ────────────────────────────────────────────────────────────

    @Test
    void testGetResumen() {
        TrabajadorView juan = trabajador(1L, "Juan", 1000.0);
        TrabajadorView ana = trabajador(2L, "Ana", 2000.0);

        when(backend.getProyectos(AUTH)).thenReturn(List.of(
                proyecto("EN_PROGRESO"), proyecto("EN_PROGRESO"), proyecto("FINALIZADO")));
        when(backend.getTareas(AUTH)).thenReturn(List.of(
                tarea("POR_HACER", "ALTA"), tarea("COMPLETADO", "BAJA"), tarea("COMPLETADO", "MEDIA")));
        when(backend.getTrabajadores(AUTH)).thenReturn(List.of(juan, ana));
        when(backend.getAsignaciones(AUTH)).thenReturn(List.of(
                asignacion(juan, 1L, 10),   // 10 * 1000 = 10000
                asignacion(ana, 2L, 5)));   //  5 * 2000 = 10000

        ResumenDTO r = analiticasService.getResumen(AUTH);

        assertEquals(3L, r.getTotalProyectos());
        assertEquals(2L, r.getProyectosActivos());
        assertEquals(2L, r.getTotalTrabajadores());
        assertEquals(1L, r.getTareasPendientes());
        assertEquals(2L, r.getTareasCompletadas());
        assertEquals(20000.0, r.getPresupuestoTotal());
    }

    @Test
    void testGetResumen_ignoraAsignacionSinTarifa() {
        TrabajadorView sinTarifa = trabajador(1L, "Juan", null);
        when(backend.getProyectos(AUTH)).thenReturn(List.of());
        when(backend.getTareas(AUTH)).thenReturn(List.of());
        when(backend.getTrabajadores(AUTH)).thenReturn(List.of(sinTarifa));
        when(backend.getAsignaciones(AUTH)).thenReturn(List.of(asignacion(sinTarifa, 1L, 10)));

        ResumenDTO r = analiticasService.getResumen(AUTH);

        assertEquals(0.0, r.getPresupuestoTotal());
    }

    // ── getProyectosPorEstado ─────────────────────────────────────────────────

    @Test
    void testGetProyectosPorEstado() {
        when(backend.getProyectos(AUTH)).thenReturn(List.of(
                proyecto("INICIO"), proyecto("INICIO"),
                proyecto("EN_PROGRESO"), proyecto("EN_PROGRESO"), proyecto("EN_PROGRESO"),
                proyecto("FINALIZADO")));

        Map<String, Long> porEstado = analiticasService.getProyectosPorEstado(AUTH).stream()
                .collect(Collectors.toMap(GrupoConteoDTO::getLabel, GrupoConteoDTO::getCantidad));

        assertEquals(3, porEstado.size());
        assertEquals(2L, porEstado.get("INICIO"));
        assertEquals(3L, porEstado.get("EN_PROGRESO"));
        assertEquals(1L, porEstado.get("FINALIZADO"));
    }

    @Test
    void testGetProyectosPorEstado_sinDatos() {
        when(backend.getProyectos(AUTH)).thenReturn(List.of());
        assertTrue(analiticasService.getProyectosPorEstado(AUTH).isEmpty());
    }

    // ── getTareasPorPrioridad / getTareasPorEstado ────────────────────────────

    @Test
    void testGetTareasPorPrioridad() {
        when(backend.getTareas(AUTH)).thenReturn(List.of(
                tarea("POR_HACER", "ALTA"), tarea("COMPLETADO", "ALTA"), tarea("EN_PROGRESO", "MEDIA")));

        Map<String, Long> porPrioridad = analiticasService.getTareasPorPrioridad(AUTH).stream()
                .collect(Collectors.toMap(GrupoConteoDTO::getLabel, GrupoConteoDTO::getCantidad));

        assertEquals(2L, porPrioridad.get("ALTA"));
        assertEquals(1L, porPrioridad.get("MEDIA"));
    }

    @Test
    void testGetTareasPorEstado() {
        when(backend.getTareas(AUTH)).thenReturn(List.of(
                tarea("POR_HACER", "ALTA"), tarea("POR_HACER", "BAJA"), tarea("REVISADO", "MEDIA")));

        Map<String, Long> porEstado = analiticasService.getTareasPorEstado(AUTH).stream()
                .collect(Collectors.toMap(GrupoConteoDTO::getLabel, GrupoConteoDTO::getCantidad));

        assertEquals(2L, porEstado.get("POR_HACER"));
        assertEquals(1L, porEstado.get("REVISADO"));
    }

    // ── getCargaTrabajo ───────────────────────────────────────────────────────

    @Test
    void testGetCargaTrabajo_ordenadoDescYTop5() {
        TrabajadorView juan = trabajador(1L, "Juan", 1000.0);
        TrabajadorView ana = trabajador(2L, "Ana", 1000.0);
        when(backend.getAsignaciones(AUTH)).thenReturn(List.of(
                asignacion(juan, 1L, 10), asignacion(juan, 2L, 30),  // Juan = 40
                asignacion(ana, 1L, 32)));                            // Ana = 32

        List<CargaTrabajoDTO> carga = analiticasService.getCargaTrabajo(AUTH);

        assertEquals(2, carga.size());
        assertEquals("Juan", carga.get(0).getNombre());
        assertEquals(40L, carga.get(0).getTotalHoras());
        assertEquals("Ana", carga.get(1).getNombre());
        assertEquals(32L, carga.get(1).getTotalHoras());
    }

    @Test
    void testGetCargaTrabajo_sinAsignaciones() {
        when(backend.getAsignaciones(AUTH)).thenReturn(List.of());
        assertTrue(analiticasService.getCargaTrabajo(AUTH).isEmpty());
    }

    // ── getCostosProyectos ────────────────────────────────────────────────────

    @Test
    void testGetCostosProyectos_ordenadoDesc() {
        TrabajadorView juan = trabajador(1L, "Juan", 1000.0);
        TrabajadorView ana = trabajador(2L, "Ana", 2000.0);
        when(backend.getAsignaciones(AUTH)).thenReturn(List.of(
                asignacion(juan, 1L, 5),    // proyecto 1: 5000
                asignacion(ana, 2L, 10)));  // proyecto 2: 20000

        List<CostoProyectoDTO> costos = analiticasService.getCostosProyectos(AUTH);

        assertEquals(2, costos.size());
        assertEquals(2L, costos.get(0).getProyectoId());
        assertEquals(20000.0, costos.get(0).getCostoEstimado());
        assertEquals(1L, costos.get(1).getProyectoId());
        assertEquals(5000.0, costos.get(1).getCostoEstimado());
    }

    @Test
    void testGetCostosProyectos_sinDatos() {
        when(backend.getAsignaciones(AUTH)).thenReturn(List.of());
        assertTrue(analiticasService.getCostosProyectos(AUTH).isEmpty());
    }

    @Test
    void testGetCostosProyectos_sumaVariasAsignacionesDelMismoProyecto() {
        TrabajadorView juan = trabajador(1L, "Juan", 1000.0);
        TrabajadorView ana = trabajador(2L, "Ana", 1000.0);
        lenient().when(backend.getAsignaciones(AUTH)).thenReturn(List.of(
                asignacion(juan, 1L, 5), asignacion(ana, 1L, 3)));  // proyecto 1: 8000

        List<CostoProyectoDTO> costos = analiticasService.getCostosProyectos(AUTH);

        assertEquals(1, costos.size());
        assertEquals(8000.0, costos.get(0).getCostoEstimado());
    }
}
