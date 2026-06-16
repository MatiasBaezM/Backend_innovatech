package Innovatech.ms_recursos_colaboraciones.service;

import Innovatech.ms_recursos_colaboraciones.model.Trabajador;
import Innovatech.ms_recursos_colaboraciones.repository.TrabajadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrabajadorServiceTest {

    @Mock private TrabajadorRepository trabajadorRepository;

    @InjectMocks
    private TrabajadorService trabajadorService;

    private Trabajador buildTrabajador(Long id, String rut, String nombre) {
        return Trabajador.builder()
                .id(id).rut(rut).nombre(nombre)
                .email(nombre.toLowerCase() + "@test.cl")
                .cargo("Developer").departamento("TI").tarifaHora(15000.0)
                .build();
    }

    @Test
    void testFindAll() {
        when(trabajadorRepository.findAll()).thenReturn(Arrays.asList(
                buildTrabajador(1L, "11.111.111-1", "Juan"),
                buildTrabajador(2L, "22.222.222-2", "Maria")
        ));

        List<Trabajador> resultado = trabajadorService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());
        verify(trabajadorRepository, times(1)).findAll();
    }

    @Test
    void testFindById_existente() {
        Trabajador trabajador = buildTrabajador(1L, "11.111.111-1", "Juan");
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));

        Optional<Trabajador> resultado = trabajadorService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Juan", resultado.get().getNombre());
    }

    @Test
    void testFindById_noExistente() {
        when(trabajadorRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Trabajador> resultado = trabajadorService.findById(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void testFindByRut() {
        Trabajador trabajador = buildTrabajador(1L, "11.111.111-1", "Juan");
        when(trabajadorRepository.findByRut("11.111.111-1")).thenReturn(Optional.of(trabajador));

        Optional<Trabajador> resultado = trabajadorService.findByRut("11.111.111-1");

        assertTrue(resultado.isPresent());
        assertEquals("11.111.111-1", resultado.get().getRut());
    }

    @Test
    void testFindByEmail() {
        Trabajador trabajador = buildTrabajador(1L, "11.111.111-1", "Juan");
        when(trabajadorRepository.findByEmail("juan@test.cl")).thenReturn(Optional.of(trabajador));

        Optional<Trabajador> resultado = trabajadorService.findByEmail("juan@test.cl");

        assertTrue(resultado.isPresent());
    }

    @Test
    void testSave() {
        Trabajador nuevo = buildTrabajador(null, "33.333.333-3", "Pedro");
        Trabajador guardado = buildTrabajador(3L, "33.333.333-3", "Pedro");
        when(trabajadorRepository.save(nuevo)).thenReturn(guardado);

        Trabajador resultado = trabajadorService.save(nuevo);

        assertEquals(3L, resultado.getId());
        verify(trabajadorRepository, times(1)).save(nuevo);
    }

    @Test
    void testDeleteById() {
        doNothing().when(trabajadorRepository).deleteById(1L);

        trabajadorService.deleteById(1L);

        verify(trabajadorRepository, times(1)).deleteById(1L);
    }
}
