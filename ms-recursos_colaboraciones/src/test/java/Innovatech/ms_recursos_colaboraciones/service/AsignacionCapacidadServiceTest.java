package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.AsignacionCapacidad;
import Innovatech.ms_recursos_colaboraciones.repository.AsignacionCapacidadRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionCapacidadServiceTest {

    @Mock private AsignacionCapacidadRepository asignacionRepository;

    @InjectMocks
    private AsignacionCapacidadService asignacionService;

    private AsignacionCapacidad buildAsignacion(Long id, Long proyectoId, Integer horas) {
        return AsignacionCapacidad.builder()
                .id(id).proyectoId(proyectoId).horasAsignadas(horas)
                .fechaInicio(LocalDate.of(2025, 1, 1))
                .fechaFin(LocalDate.of(2025, 6, 1))
                .rolEnProyecto("Frontend Developer")
                .build();
    }

    @Test
    void testFindAll() {
        when(asignacionRepository.findAll()).thenReturn(Arrays.asList(
                buildAsignacion(1L, 100L, 20),
                buildAsignacion(2L, 200L, 30)
        ));

        List<AsignacionCapacidad> resultado = asignacionService.findAll();

        assertEquals(2, resultado.size());
        verify(asignacionRepository, times(1)).findAll();
    }

    @Test
    void testFindByTrabajadorId() {
        when(asignacionRepository.findByTrabajadorId(5L)).thenReturn(List.of(
                buildAsignacion(1L, 100L, 20)
        ));

        List<AsignacionCapacidad> resultado = asignacionService.findByTrabajadorId(5L);

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).getProyectoId());
    }

    @Test
    void testFindByProyectoId() {
        when(asignacionRepository.findByProyectoId(100L)).thenReturn(List.of(
                buildAsignacion(1L, 100L, 20),
                buildAsignacion(2L, 100L, 10)
        ));

        List<AsignacionCapacidad> resultado = asignacionService.findByProyectoId(100L);

        assertEquals(2, resultado.size());
    }

    @Test
    void testFindById_existente() {
        when(asignacionRepository.findById(1L)).thenReturn(Optional.of(buildAsignacion(1L, 100L, 20)));

        Optional<AsignacionCapacidad> resultado = asignacionService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(20, resultado.get().getHorasAsignadas());
    }

    @Test
    void testFindById_noExistente() {
        when(asignacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(asignacionService.findById(99L).isEmpty());
    }

    @Test
    void testSave() {
        AsignacionCapacidad nueva = buildAsignacion(null, 100L, 20);
        AsignacionCapacidad guardada = buildAsignacion(1L, 100L, 20);
        when(asignacionRepository.save(nueva)).thenReturn(guardada);

        AsignacionCapacidad resultado = asignacionService.save(nueva);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void testSaveAll() {
        List<AsignacionCapacidad> nuevas = Arrays.asList(
                buildAsignacion(null, 100L, 20),
                buildAsignacion(null, 200L, 30)
        );
        when(asignacionRepository.saveAll(nuevas)).thenReturn(Arrays.asList(
                buildAsignacion(1L, 100L, 20),
                buildAsignacion(2L, 200L, 30)
        ));

        List<AsignacionCapacidad> resultado = asignacionService.saveAll(nuevas);

        assertEquals(2, resultado.size());
        verify(asignacionRepository, times(1)).saveAll(nuevas);
    }

    @Test
    void testDeleteById() {
        doNothing().when(asignacionRepository).deleteById(1L);

        asignacionService.deleteById(1L);

        verify(asignacionRepository, times(1)).deleteById(1L);
    }
}
