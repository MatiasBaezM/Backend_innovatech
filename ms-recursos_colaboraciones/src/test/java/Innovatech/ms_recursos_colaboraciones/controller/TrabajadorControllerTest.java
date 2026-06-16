package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.Trabajador;
import Innovatech.ms_recursos_colaboraciones.service.TrabajadorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrabajadorController.class)
@ActiveProfiles("test")
class TrabajadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrabajadorService trabajadorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Trabajador buildTrabajador(Long id, String rut, String nombre) {
        return Trabajador.builder()
                .id(id).rut(rut).nombre(nombre)
                .email(nombre.toLowerCase() + "@test.cl")
                .cargo("Developer").departamento("TI").tarifaHora(15000.0)
                .build();
    }

    @Test
    void testGetAllTrabajadores() throws Exception {
        Mockito.when(trabajadorService.findAll()).thenReturn(Arrays.asList(
                buildTrabajador(1L, "11.111.111-1", "Juan"),
                buildTrabajador(2L, "22.222.222-2", "Maria")
        ));

        mockMvc.perform(get("/api/trabajadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Juan")));
    }

    @Test
    void testGetTrabajadorById_existente() throws Exception {
        Mockito.when(trabajadorService.findById(1L)).thenReturn(Optional.of(buildTrabajador(1L, "11.111.111-1", "Juan")));

        mockMvc.perform(get("/api/trabajadores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void testGetTrabajadorById_noExistente() throws Exception {
        Mockito.when(trabajadorService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trabajadores/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTrabajadorByRut_existente() throws Exception {
        Mockito.when(trabajadorService.findByRut("11.111.111-1")).thenReturn(Optional.of(buildTrabajador(1L, "11.111.111-1", "Juan")));

        mockMvc.perform(get("/api/trabajadores/rut/11.111.111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("11.111.111-1"));
    }

    @Test
    void testGetTrabajadorByRut_noExistente() throws Exception {
        Mockito.when(trabajadorService.findByRut("99.999.999-9")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trabajadores/rut/99.999.999-9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTrabajador() throws Exception {
        Trabajador nuevo = buildTrabajador(null, "33.333.333-3", "Pedro");
        Trabajador guardado = buildTrabajador(3L, "33.333.333-3", "Pedro");
        Mockito.when(trabajadorService.save(any(Trabajador.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/trabajadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.nombre").value("Pedro"));
    }

    @Test
    void testUpdateTrabajador_existente() throws Exception {
        Trabajador existente = buildTrabajador(1L, "11.111.111-1", "Juan");
        Trabajador datos = buildTrabajador(null, "11.111.111-1", "Juan Actualizado");
        Mockito.when(trabajadorService.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(trabajadorService.save(any(Trabajador.class))).thenReturn(buildTrabajador(1L, "11.111.111-1", "Juan Actualizado"));

        mockMvc.perform(put("/api/trabajadores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Actualizado"));
    }

    @Test
    void testUpdateTrabajador_noExistente() throws Exception {
        Trabajador datos = buildTrabajador(null, "99.999.999-9", "Ghost");
        Mockito.when(trabajadorService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/trabajadores/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTrabajador_existente() throws Exception {
        Mockito.when(trabajadorService.findById(1L)).thenReturn(Optional.of(buildTrabajador(1L, "11.111.111-1", "Juan")));
        Mockito.doNothing().when(trabajadorService).deleteById(1L);

        mockMvc.perform(delete("/api/trabajadores/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTrabajador_noExistente() throws Exception {
        Mockito.when(trabajadorService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/trabajadores/99"))
                .andExpect(status().isNotFound());
    }
}
