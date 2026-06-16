package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.model.Colaborador;
import Innovatech.ms_gestion_proyectos.model.EquipoRequest;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.repository.ProyectoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    private Proyecto buildProyecto(Long id, String nombre, Long gestorId, List<Colaborador> colaboradores) {
        return Proyecto.builder()
                .id(id).nombre(nombre).descripcion("Descripción")
                .estado("EN_PROGRESO").gestorId(gestorId)
                .colaboradores(colaboradores != null ? colaboradores : List.of())
                .build();
    }

    // ── getAllProyectos ────────────────────────────────────────────────────────

    @Test
    void testGetAllProyectos_comoAdministrador_retornaTodos() {
        List<Proyecto> todos = Arrays.asList(
                buildProyecto(1L, "Proyecto A", 5L, List.of(new Colaborador(10L, "Juan"))),
                buildProyecto(2L, "Proyecto B", 6L, List.of(new Colaborador(20L, "Maria")))
        );
        when(proyectoRepository.findAll()).thenReturn(todos);

        List<Proyecto> resultado = proyectoService.getAllProyectos("ADMINISTRADOR", 1L);

        assertEquals(2, resultado.size());
        verify(proyectoRepository, times(1)).findAll();
    }

    @Test
    void testGetAllProyectos_comoGestor_soloSusProyectos() {
        List<Proyecto> todos = Arrays.asList(
                buildProyecto(1L, "Proyecto A", 5L, List.of()),
                buildProyecto(2L, "Proyecto B", 6L, List.of())
        );
        when(proyectoRepository.findAll()).thenReturn(todos);

        List<Proyecto> resultado = proyectoService.getAllProyectos("GESTOR_PROYECTOS", 5L);

        assertEquals(1, resultado.size());
        assertEquals("Proyecto A", resultado.get(0).getNombre());
        assertEquals(5L, resultado.get(0).getGestorId());
    }

    @Test
    void testGetAllProyectos_comoColaborador_soloSusProyectos() {
        List<Proyecto> todos = Arrays.asList(
                buildProyecto(1L, "Proyecto A", 5L, List.of(new Colaborador(10L, "Juan"))),
                buildProyecto(2L, "Proyecto B", 6L, List.of(new Colaborador(20L, "Maria")))
        );
        when(proyectoRepository.findAll()).thenReturn(todos);

        List<Proyecto> resultado = proyectoService.getAllProyectos("COLABORADOR", 10L);

        assertEquals(1, resultado.size());
        assertEquals("Proyecto A", resultado.get(0).getNombre());
    }

    @Test
    void testGetAllProyectos_colaboradorSinProyectos_retornaVacio() {
        List<Proyecto> todos = List.of(
                buildProyecto(1L, "Proyecto A", 5L, List.of(new Colaborador(10L, "Juan")))
        );
        when(proyectoRepository.findAll()).thenReturn(todos);

        List<Proyecto> resultado = proyectoService.getAllProyectos("COLABORADOR", 99L);

        assertTrue(resultado.isEmpty());
    }

    // ── getProyectoById ───────────────────────────────────────────────────────

    @Test
    void testGetProyectoById_existente() {
        Proyecto proyecto = buildProyecto(1L, "Proyecto A", 5L, List.of());
        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(proyecto));

        Optional<Proyecto> resultado = proyectoService.getProyectoById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Proyecto A", resultado.get().getNombre());
        verify(proyectoRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProyectoById_noExistente() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Proyecto> resultado = proyectoService.getProyectoById(99L);

        assertFalse(resultado.isPresent());
    }

    // ── createProyecto ────────────────────────────────────────────────────────

    @Test
    void testCreateProyecto() {
        Proyecto nuevo = buildProyecto(null, "Nuevo Proyecto", null, null);
        Proyecto guardado = buildProyecto(1L, "Nuevo Proyecto", null, List.of());

        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(guardado);

        Proyecto resultado = proyectoService.createProyecto(nuevo);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Nuevo Proyecto", resultado.getNombre());
        verify(proyectoRepository, times(1)).save(any(Proyecto.class));
    }

    // ── updateProyecto ────────────────────────────────────────────────────────

    @Test
    void testUpdateProyecto_exitoso() {
        Proyecto existente = buildProyecto(1L, "Proyecto Viejo", 5L, List.of());
        Proyecto datos = buildProyecto(null, "Proyecto Nuevo", 5L, List.of());
        datos.setEstado("FINALIZADO");
        Proyecto actualizado = buildProyecto(1L, "Proyecto Nuevo", 5L, List.of());
        actualizado.setEstado("FINALIZADO");

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(actualizado);

        Proyecto resultado = proyectoService.updateProyecto(1L, datos);

        assertNotNull(resultado);
        assertEquals("Proyecto Nuevo", resultado.getNombre());
        assertEquals("FINALIZADO", resultado.getEstado());
        verify(proyectoRepository, times(1)).save(any(Proyecto.class));
    }

    @Test
    void testUpdateProyecto_noEncontrado_lanzaExcepcion() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());
        Proyecto datos = buildProyecto(null, "Datos", null, null);

        assertThrows(RuntimeException.class, () -> proyectoService.updateProyecto(99L, datos));
        verify(proyectoRepository, never()).save(any());
    }

    // ── updateEquipo ──────────────────────────────────────────────────────────

    @Test
    void testUpdateEquipo_exitoso() {
        Proyecto existente = buildProyecto(1L, "Proyecto A", null, List.of());
        List<Colaborador> colaboradores = List.of(
                new Colaborador(10L, "Juan"),
                new Colaborador(20L, "Maria")
        );
        EquipoRequest request = new EquipoRequest();
        request.setGestorId(5L);
        request.setGestorNombre("Pedro Gestor");
        request.setColaboradores(colaboradores);

        Proyecto actualizado = buildProyecto(1L, "Proyecto A", 5L, colaboradores);
        actualizado.setGestorNombre("Pedro Gestor");

        when(proyectoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proyectoRepository.save(any(Proyecto.class))).thenReturn(actualizado);

        Proyecto resultado = proyectoService.updateEquipo(1L, request);

        assertNotNull(resultado);
        assertEquals(5L, resultado.getGestorId());
        assertEquals("Pedro Gestor", resultado.getGestorNombre());
        assertEquals(2, resultado.getColaboradores().size());
        verify(proyectoRepository, times(1)).save(any(Proyecto.class));
    }

    @Test
    void testUpdateEquipo_proyectoNoEncontrado_lanzaExcepcion() {
        when(proyectoRepository.findById(99L)).thenReturn(Optional.empty());
        EquipoRequest request = new EquipoRequest();
        request.setGestorId(5L);

        assertThrows(RuntimeException.class, () -> proyectoService.updateEquipo(99L, request));
        verify(proyectoRepository, never()).save(any());
    }

    // ── deleteProyecto ────────────────────────────────────────────────────────

    @Test
    void testDeleteProyecto() {
        doNothing().when(proyectoRepository).deleteById(1L);

        proyectoService.deleteProyecto(1L);

        verify(proyectoRepository, times(1)).deleteById(1L);
    }
}
