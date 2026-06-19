package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.dto.GestorActividadResponse;
import Innovatech.ms_gestion_proyectos.model.Actividad;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
import Innovatech.ms_gestion_proyectos.repository.ProyectoRepository;
import Innovatech.ms_gestion_proyectos.repository.TareaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActividadServiceTest {

    @Mock private ActividadRepository actividadRepository;
    @Mock private ProyectoRepository proyectoRepository;
    @Mock private TareaRepository tareaRepository;

    @InjectMocks
    private ActividadService actividadService;

    private Proyecto proyecto(Long id, Long gestorId, String nombre) {
        return Proyecto.builder().id(id).gestorId(gestorId).nombre(nombre).build();
    }

    private Tarea tareaCompletada(Long id, Long proyectoId, String titulo, String asignado) {
        return Tarea.builder().id(id).proyectoId(proyectoId).titulo(titulo)
                .asignadoNombre(asignado).estado(Tarea.Estado.COMPLETADO).build();
    }

    @Test
    void testFeedGestor_filtraSusProyectosYListaPorAprobar() {
        // proyectos 1 y 3 son del gestor 7; el 2 es de otro gestor
        List<Proyecto> proyectos = List.of(
                proyecto(1L, 7L, "Proyecto A"),
                proyecto(2L, 9L, "Proyecto Ajeno"),
                proyecto(3L, 7L, "Proyecto C"));
        when(proyectoRepository.findAll()).thenReturn(proyectos);
        when(actividadRepository.findTop30ByProyectoIdInOrderByFechaCreacionDesc(any()))
                .thenReturn(List.of(new Actividad()));
        when(tareaRepository.findByProyectoIdInAndEstado(any(), eq(Tarea.Estado.COMPLETADO)))
                .thenReturn(List.of(tareaCompletada(5L, 1L, "Login", "Juan")));

        GestorActividadResponse r = actividadService.getFeedGestor("GESTOR_PROYECTOS", 7L);

        // Solo deben consultarse los proyectos 1 y 3
        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(tareaRepository).findByProyectoIdInAndEstado(idsCaptor.capture(), eq(Tarea.Estado.COMPLETADO));
        assertEquals(List.of(1L, 3L), idsCaptor.getValue());

        assertEquals(1, r.getPorAprobar().size());
        assertEquals("Login", r.getPorAprobar().get(0).getTitulo());
        assertEquals("Proyecto A", r.getPorAprobar().get(0).getProyectoNombre());
        assertEquals("Juan", r.getPorAprobar().get(0).getAsignadoNombre());
    }

    @Test
    void testFeedGestor_sinProyectos_devuelveVacio() {
        when(proyectoRepository.findAll()).thenReturn(List.of(proyecto(1L, 9L, "Ajeno")));

        GestorActividadResponse r = actividadService.getFeedGestor("GESTOR_PROYECTOS", 7L);

        assertTrue(r.getActividades().isEmpty());
        assertTrue(r.getPorAprobar().isEmpty());
        verify(actividadRepository, never()).findTop30ByProyectoIdInOrderByFechaCreacionDesc(any());
    }

    @Test
    void testFeedGestor_adminVeTodosLosProyectos() {
        List<Proyecto> proyectos = List.of(
                proyecto(1L, 7L, "Proyecto A"),
                proyecto(2L, 9L, "Proyecto B"));
        when(proyectoRepository.findAll()).thenReturn(proyectos);
        when(actividadRepository.findTop30ByProyectoIdInOrderByFechaCreacionDesc(any()))
                .thenReturn(List.of());
        when(tareaRepository.findByProyectoIdInAndEstado(any(), any())).thenReturn(List.of());

        actividadService.getFeedGestor("ADMINISTRADOR", 1L);

        ArgumentCaptor<List<Long>> idsCaptor = ArgumentCaptor.forClass(List.class);
        verify(tareaRepository).findByProyectoIdInAndEstado(idsCaptor.capture(), any());
        assertEquals(List.of(1L, 2L), idsCaptor.getValue());
    }
}
