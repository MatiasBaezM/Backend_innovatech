package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.model.Colaborador;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.model.Tarea.Estado;
import Innovatech.ms_gestion_proyectos.model.Tarea.Prioridad;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
import Innovatech.ms_gestion_proyectos.repository.ProyectoRepository;
import Innovatech.ms_gestion_proyectos.repository.TareaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock private TareaRepository tareaRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private ActividadRepository actividadRepository;

    @InjectMocks
    private TareaService tareaService;

    private Tarea buildTarea(Long id, Long proyectoId, Estado estado) {
        return Tarea.builder()
                .id(id).proyectoId(proyectoId)
                .titulo("Tarea de prueba").descripcion("Descripción")
                .prioridad(Prioridad.MEDIA).estado(estado)
                .asignadoId(10L).asignadoNombre("Juan")
                .fechaCreacion(LocalDate.now()).fechaLimite(LocalDate.now().plusDays(7))
                .build();
    }

    private Proyecto buildProyecto(Long id, Long gestorId) {
        return Proyecto.builder()
                .id(id).nombre("Proyecto Test").gestorId(gestorId)
                .colaboradores(List.of(new Colaborador(10L, "Juan")))
                .build();
    }

    // ── getTareasByProyecto ────────────────────────────────────────────────────

    @Test
    void testGetTareasByProyecto() {
        List<Tarea> tareas = Arrays.asList(
                buildTarea(1L, 5L, Estado.POR_HACER),
                buildTarea(2L, 5L, Estado.EN_PROGRESO)
        );
        when(tareaRepository.findByProyectoId(5L)).thenReturn(tareas);

        List<Tarea> resultado = tareaService.getTareasByProyecto(5L);

        assertEquals(2, resultado.size());
        assertEquals(Estado.POR_HACER, resultado.get(0).getEstado());
        verify(tareaRepository, times(1)).findByProyectoId(5L);
    }

    // ── getTareaById ──────────────────────────────────────────────────────────

    @Test
    void testGetTareaById_existente() {
        Tarea tarea = buildTarea(1L, 5L, Estado.POR_HACER);
        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        Optional<Tarea> resultado = tareaService.getTareaById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(tareaRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTareaById_noExistente() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Tarea> resultado = tareaService.getTareaById(99L);

        assertFalse(resultado.isPresent());
        verify(tareaRepository, times(1)).findById(99L);
    }

    // ── createTarea ───────────────────────────────────────────────────────────

    @Test
    void testCreateTarea_comoAdministrador() {
        Tarea nueva = buildTarea(null, 5L, Estado.POR_HACER);
        Tarea guardada = buildTarea(1L, 5L, Estado.POR_HACER);

        when(tareaRepository.save(any(Tarea.class))).thenReturn(guardada);

        Tarea resultado = tareaService.createTarea(5L, nueva, "ADMINISTRADOR", 99L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(5L, resultado.getProyectoId());
        verify(tareaRepository, times(1)).save(any(Tarea.class));
        // ADMINISTRADOR no dispara el chequeo de autorizacion del gestor; el unico
        // findById que ocurre es el del registro de actividad (lookup del nombre del proyecto).
        verify(actividadRepository, times(1)).save(any());
    }

    @Test
    void testCreateTarea_comoGestorPropioProyecto() {
        Proyecto proyecto = buildProyecto(5L, 7L);
        Tarea nueva = buildTarea(null, 5L, Estado.POR_HACER);
        Tarea guardada = buildTarea(1L, 5L, Estado.POR_HACER);

        when(proyectoRepository.findById(5L)).thenReturn(Optional.of(proyecto));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(guardada);

        Tarea resultado = tareaService.createTarea(5L, nueva, "GESTOR_PROYECTOS", 7L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        // findById ocurre dos veces: una para autorizar al gestor y otra en el
        // registro de actividad (lookup del nombre del proyecto).
        verify(proyectoRepository, times(2)).findById(5L);
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testCreateTarea_comoGestorProyectoAjeno_lanzaSecurityException() {
        Proyecto proyecto = buildProyecto(5L, 7L);
        Tarea nueva = buildTarea(null, 5L, Estado.POR_HACER);

        when(proyectoRepository.findById(5L)).thenReturn(Optional.of(proyecto));

        assertThrows(SecurityException.class,
                () -> tareaService.createTarea(5L, nueva, "GESTOR_PROYECTOS", 999L));

        verify(tareaRepository, never()).save(any());
    }

    @Test
    void testCreateTarea_proyectoNoEncontrado_lanzaExcepcion() {
        Tarea nueva = buildTarea(null, 99L, Estado.POR_HACER);
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> tareaService.createTarea(99L, nueva, "GESTOR_PROYECTOS", 7L));

        verify(tareaRepository, never()).save(any());
    }

    // ── updateTarea ───────────────────────────────────────────────────────────

    @Test
    void testUpdateTarea_exitoso() {
        Tarea existente = buildTarea(1L, 5L, Estado.POR_HACER);
        Tarea datos = buildTarea(null, 5L, Estado.EN_PROGRESO);
        datos.setTitulo("Título actualizado");
        Tarea actualizada = buildTarea(1L, 5L, Estado.EN_PROGRESO);
        actualizada.setTitulo("Título actualizado");

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(actualizada);

        Tarea resultado = tareaService.updateTarea(1L, datos);

        assertNotNull(resultado);
        assertEquals(Estado.EN_PROGRESO, resultado.getEstado());
        assertEquals("Título actualizado", resultado.getTitulo());
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testUpdateTarea_noEncontrada_lanzaExcepcion() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());
        Tarea datos = buildTarea(null, 5L, Estado.EN_PROGRESO);

        assertThrows(RuntimeException.class, () -> tareaService.updateTarea(99L, datos));
        verify(tareaRepository, never()).save(any());
    }

    // ── aprobarTarea ──────────────────────────────────────────────────────────

    @Test
    void testAprobarTarea_pasaARevisado() {
        Tarea tarea = buildTarea(1L, 5L, Estado.COMPLETADO);
        tarea.setMensajeCorreccion("Mensaje previo");
        Tarea aprobada = buildTarea(1L, 5L, Estado.REVISADO);

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(aprobada);

        Tarea resultado = tareaService.aprobarTarea(1L);

        assertNotNull(resultado);
        assertEquals(Estado.REVISADO, resultado.getEstado());
        assertNull(resultado.getMensajeCorreccion());
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testAprobarTarea_noEncontrada_lanzaExcepcion() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tareaService.aprobarTarea(99L));
    }

    // ── rechazarTarea ─────────────────────────────────────────────────────────

    @Test
    void testRechazarTarea_vuelveAPorHacerConMensaje() {
        Tarea tarea = buildTarea(1L, 5L, Estado.COMPLETADO);
        Tarea rechazada = buildTarea(1L, 5L, Estado.POR_HACER);
        rechazada.setMensajeCorreccion("Falta documentación");

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));
        when(tareaRepository.save(any(Tarea.class))).thenReturn(rechazada);

        Tarea resultado = tareaService.rechazarTarea(1L, "Falta documentación");

        assertNotNull(resultado);
        assertEquals(Estado.POR_HACER, resultado.getEstado());
        assertEquals("Falta documentación", resultado.getMensajeCorreccion());
        verify(tareaRepository, times(1)).save(any(Tarea.class));
    }

    @Test
    void testRechazarTarea_noEncontrada_lanzaExcepcion() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> tareaService.rechazarTarea(99L, "Mensaje"));
    }

    // ── deleteTarea ───────────────────────────────────────────────────────────

    @Test
    void testDeleteTarea() {
        doNothing().when(tareaRepository).deleteById(1L);

        tareaService.deleteTarea(1L);

        verify(tareaRepository, times(1)).deleteById(1L);
    }
}
