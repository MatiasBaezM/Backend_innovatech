package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.Habilidad;
import Innovatech.ms_recursos_colaboraciones.repository.HabilidadRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabilidadServiceTest {

    @Mock private HabilidadRepository habilidadRepository;
    @Mock private EntityManager entityManager;
    @Mock private Query mockQuery;

    @InjectMocks
    private HabilidadService habilidadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(habilidadService, "entityManager", entityManager);
    }

    private Habilidad buildHabilidad(Long id, String nombre, String color) {
        return Habilidad.builder().id(id).nombre(nombre).color(color).build();
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void testFindAll() {
        List<Habilidad> habilidades = Arrays.asList(
                buildHabilidad(1L, "Desarrollador Backend", "#6366f1"),
                buildHabilidad(2L, "Desarrollador Frontend", "#0ea5e9"),
                buildHabilidad(3L, "DBA", "#10b981")
        );
        when(habilidadRepository.findAll()).thenReturn(habilidades);

        List<Habilidad> resultado = habilidadService.findAll();

        assertEquals(3, resultado.size());
        assertEquals("Desarrollador Backend", resultado.get(0).getNombre());
        assertEquals("#0ea5e9", resultado.get(1).getColor());
        verify(habilidadRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_listaVacia() {
        when(habilidadRepository.findAll()).thenReturn(List.of());

        List<Habilidad> resultado = habilidadService.findAll();

        assertTrue(resultado.isEmpty());
        verify(habilidadRepository, times(1)).findAll();
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void testFindById_existente() {
        Habilidad habilidad = buildHabilidad(1L, "DevOps", "#ef4444");
        when(habilidadRepository.findById(1L)).thenReturn(Optional.of(habilidad));

        Optional<Habilidad> resultado = habilidadService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        assertEquals("DevOps", resultado.get().getNombre());
        assertEquals("#ef4444", resultado.get().getColor());
        verify(habilidadRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_noExistente() {
        when(habilidadRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Habilidad> resultado = habilidadService.findById(99L);

        assertFalse(resultado.isPresent());
        verify(habilidadRepository, times(1)).findById(99L);
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    void testSave_nuevaHabilidad() {
        Habilidad nueva = buildHabilidad(null, "Scrum Master", "#8b5cf6");
        Habilidad guardada = buildHabilidad(7L, "Scrum Master", "#8b5cf6");

        when(habilidadRepository.save(nueva)).thenReturn(guardada);

        Habilidad resultado = habilidadService.save(nueva);

        assertNotNull(resultado);
        assertEquals(7L, resultado.getId());
        assertEquals("Scrum Master", resultado.getNombre());
        assertEquals("#8b5cf6", resultado.getColor());
        verify(habilidadRepository, times(1)).save(nueva);
    }

    @Test
    void testSave_actualizarHabilidad() {
        Habilidad existente = buildHabilidad(1L, "Backend Viejo", "#000000");
        Habilidad actualizada = buildHabilidad(1L, "Desarrollador Backend", "#6366f1");

        when(habilidadRepository.save(existente)).thenReturn(actualizada);

        Habilidad resultado = habilidadService.save(existente);

        assertNotNull(resultado);
        assertEquals("Desarrollador Backend", resultado.getNombre());
        assertEquals("#6366f1", resultado.getColor());
        verify(habilidadRepository, times(1)).save(existente);
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    void testDeleteById_limpiaRelacionesYElimina() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1);
        doNothing().when(habilidadRepository).deleteById(1L);

        habilidadService.deleteById(1L);

        // Verifica que se limpian ambas tablas de relación antes de eliminar
        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(mockQuery, times(2)).setParameter(eq("id"), eq(1L));
        verify(mockQuery, times(2)).executeUpdate();
        verify(habilidadRepository, times(1)).deleteById(1L);
    }
}
