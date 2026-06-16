package Innovatech.ms_analiticas.service;

import Innovatech.ms_analiticas.dto.CargaTrabajoDTO;
import Innovatech.ms_analiticas.dto.CostoProyectoDTO;
import Innovatech.ms_analiticas.dto.GrupoConteoDTO;
import Innovatech.ms_analiticas.dto.ResumenDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliticasServiceTest {

    @Mock private EntityManager em;
    @Mock private Query mockQuery;

    private AnaliticasService analiticasService;

    @BeforeEach
    void setUp() {
        analiticasService = new AnaliticasService();
        ReflectionTestUtils.setField(analiticasService, "em", em);
        // Todas las queries nativas pasan por el mismo mock
        when(em.createNativeQuery(anyString())).thenReturn(mockQuery);
    }

    // ── getResumen ────────────────────────────────────────────────────────────

    @Test
    void testGetResumen() {
        // 5 queries COUNT seguidas de 1 query de presupuesto (SUM)
        when(mockQuery.getSingleResult())
                .thenReturn(10L)   // totalProyectos
                .thenReturn(4L)    // proyectosActivos
                .thenReturn(8L)    // totalTrabajadores
                .thenReturn(15L)   // tareasPendientes
                .thenReturn(30L)   // tareasCompletadas
                .thenReturn(5000.0); // presupuesto

        ResumenDTO resultado = analiticasService.getResumen();

        assertNotNull(resultado);
        assertEquals(10L, resultado.getTotalProyectos());
        assertEquals(4L, resultado.getProyectosActivos());
        assertEquals(8L, resultado.getTotalTrabajadores());
        assertEquals(15L, resultado.getTareasPendientes());
        assertEquals(30L, resultado.getTareasCompletadas());
        assertEquals(5000.0, resultado.getPresupuestoTotal());
        verify(em, times(6)).createNativeQuery(anyString());
    }

    // ── getProyectosPorEstado ─────────────────────────────────────────────────

    @Test
    void testGetProyectosPorEstado() {
        List<Object[]> rows = Arrays.asList(
                new Object[]{"INICIO", 2L},
                new Object[]{"EN_PROGRESO", 5L},
                new Object[]{"FINALIZADO", 3L}
        );
        when(mockQuery.getResultList()).thenReturn(rows);

        List<GrupoConteoDTO> resultado = analiticasService.getProyectosPorEstado();

        assertEquals(3, resultado.size());
        assertEquals("INICIO", resultado.get(0).getLabel());
        assertEquals(2L, resultado.get(0).getCantidad());
        assertEquals("EN_PROGRESO", resultado.get(1).getLabel());
        assertEquals(5L, resultado.get(1).getCantidad());
        assertEquals("FINALIZADO", resultado.get(2).getLabel());
        verify(em, times(1)).createNativeQuery(anyString());
    }

    @Test
    void testGetProyectosPorEstado_sinDatos() {
        when(mockQuery.getResultList()).thenReturn(List.of());

        List<GrupoConteoDTO> resultado = analiticasService.getProyectosPorEstado();

        assertTrue(resultado.isEmpty());
    }

    // ── getTareasPorPrioridad ─────────────────────────────────────────────────

    @Test
    void testGetTareasPorPrioridad() {
        List<Object[]> rows = Arrays.asList(
                new Object[]{"ALTA", 10L},
                new Object[]{"MEDIA", 25L},
                new Object[]{"BAJA", 8L}
        );
        when(mockQuery.getResultList()).thenReturn(rows);

        List<GrupoConteoDTO> resultado = analiticasService.getTareasPorPrioridad();

        assertEquals(3, resultado.size());
        assertEquals("ALTA", resultado.get(0).getLabel());
        assertEquals(10L, resultado.get(0).getCantidad());
        assertEquals("MEDIA", resultado.get(1).getLabel());
        assertEquals(25L, resultado.get(1).getCantidad());
        verify(em, times(1)).createNativeQuery(anyString());
    }

    // ── getTareasPorEstado ────────────────────────────────────────────────────

    @Test
    void testGetTareasPorEstado() {
        List<Object[]> rows = Arrays.asList(
                new Object[]{"POR_HACER", 5L},
                new Object[]{"EN_PROGRESO", 12L},
                new Object[]{"COMPLETADO", 8L},
                new Object[]{"REVISADO", 20L}
        );
        when(mockQuery.getResultList()).thenReturn(rows);

        List<GrupoConteoDTO> resultado = analiticasService.getTareasPorEstado();

        assertEquals(4, resultado.size());
        assertEquals("REVISADO", resultado.get(3).getLabel());
        assertEquals(20L, resultado.get(3).getCantidad());
        verify(em, times(1)).createNativeQuery(anyString());
    }

    // ── getCargaTrabajo ───────────────────────────────────────────────────────

    @Test
    void testGetCargaTrabajo() {
        List<Object[]> rows = Arrays.asList(
                new Object[]{"Juan Pérez", 40L},
                new Object[]{"María Silva", 32L}
        );
        when(mockQuery.getResultList()).thenReturn(rows);

        List<CargaTrabajoDTO> resultado = analiticasService.getCargaTrabajo();

        assertEquals(2, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
        assertEquals(40L, resultado.get(0).getTotalHoras());
        assertEquals("María Silva", resultado.get(1).getNombre());
        assertEquals(32L, resultado.get(1).getTotalHoras());
        verify(em, times(1)).createNativeQuery(anyString());
    }

    @Test
    void testGetCargaTrabajo_sinAsignaciones() {
        when(mockQuery.getResultList()).thenReturn(List.of());

        List<CargaTrabajoDTO> resultado = analiticasService.getCargaTrabajo();

        assertTrue(resultado.isEmpty());
    }

    // ── getCostosProyectos ────────────────────────────────────────────────────

    @Test
    void testGetCostosProyectos() {
        List<Object[]> rows = Arrays.asList(
                new Object[]{1L, 12500.0},
                new Object[]{2L, 8000.0},
                new Object[]{3L, 3200.0}
        );
        when(mockQuery.getResultList()).thenReturn(rows);

        List<CostoProyectoDTO> resultado = analiticasService.getCostosProyectos();

        assertEquals(3, resultado.size());
        assertEquals(1L, resultado.get(0).getProyectoId());
        assertEquals(12500.0, resultado.get(0).getCostoEstimado());
        assertEquals(2L, resultado.get(1).getProyectoId());
        assertEquals(8000.0, resultado.get(1).getCostoEstimado());
        verify(em, times(1)).createNativeQuery(anyString());
    }

    @Test
    void testGetCostosProyectos_sinDatos() {
        when(mockQuery.getResultList()).thenReturn(List.of());

        List<CostoProyectoDTO> resultado = analiticasService.getCostosProyectos();

        assertTrue(resultado.isEmpty());
    }
}
