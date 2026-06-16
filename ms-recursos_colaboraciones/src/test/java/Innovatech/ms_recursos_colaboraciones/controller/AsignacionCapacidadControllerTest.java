package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.AsignacionCapacidad;
import Innovatech.ms_recursos_colaboraciones.service.AsignacionCapacidadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AsignacionCapacidadController.class)
@ActiveProfiles("test")
class AsignacionCapacidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AsignacionCapacidadService asignacionService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private AsignacionCapacidad buildAsignacion(Long id, Long proyectoId, Integer horas) {
        return AsignacionCapacidad.builder()
                .id(id).proyectoId(proyectoId).horasAsignadas(horas)
                .fechaInicio(LocalDate.of(2025, 1, 1))
                .fechaFin(LocalDate.of(2025, 6, 1))
                .rolEnProyecto("Frontend Developer")
                .build();
    }

    @Test
    void testGetAllAsignaciones() throws Exception {
        Mockito.when(asignacionService.findAll()).thenReturn(Arrays.asList(
                buildAsignacion(1L, 100L, 20),
                buildAsignacion(2L, 200L, 30)
        ));

        mockMvc.perform(get("/api/asignaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetAsignacionesByTrabajador() throws Exception {
        Mockito.when(asignacionService.findByTrabajadorId(5L)).thenReturn(List.of(buildAsignacion(1L, 100L, 20)));

        mockMvc.perform(get("/api/asignaciones/trabajador/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].proyectoId").value(100L));
    }

    @Test
    void testGetAsignacionesByProyecto() throws Exception {
        Mockito.when(asignacionService.findByProyectoId(100L)).thenReturn(Arrays.asList(
                buildAsignacion(1L, 100L, 20),
                buildAsignacion(2L, 100L, 10)
        ));

        mockMvc.perform(get("/api/asignaciones/proyecto/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testCreateAsignacion() throws Exception {
        AsignacionCapacidad nueva = buildAsignacion(null, 100L, 20);
        Mockito.when(asignacionService.save(any(AsignacionCapacidad.class))).thenReturn(buildAsignacion(1L, 100L, 20));

        mockMvc.perform(post("/api/asignaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.horasAsignadas").value(20));
    }

    @Test
    void testCreateAsignacionesBatch() throws Exception {
        List<AsignacionCapacidad> nuevas = Arrays.asList(
                buildAsignacion(null, 100L, 20),
                buildAsignacion(null, 200L, 30)
        );
        Mockito.when(asignacionService.saveAll(anyList())).thenReturn(Arrays.asList(
                buildAsignacion(1L, 100L, 20),
                buildAsignacion(2L, 200L, 30)
        ));

        mockMvc.perform(post("/api/asignaciones/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevas)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testUpdateAsignacion_existente() throws Exception {
        AsignacionCapacidad existente = buildAsignacion(1L, 100L, 20);
        AsignacionCapacidad datos = buildAsignacion(null, 100L, 40);
        Mockito.when(asignacionService.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(asignacionService.save(any(AsignacionCapacidad.class))).thenReturn(buildAsignacion(1L, 100L, 40));

        mockMvc.perform(put("/api/asignaciones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horasAsignadas").value(40));
    }

    @Test
    void testUpdateAsignacion_noExistente() throws Exception {
        AsignacionCapacidad datos = buildAsignacion(null, 100L, 40);
        Mockito.when(asignacionService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/asignaciones/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAsignacion_existente() throws Exception {
        Mockito.when(asignacionService.findById(1L)).thenReturn(Optional.of(buildAsignacion(1L, 100L, 20)));
        Mockito.doNothing().when(asignacionService).deleteById(1L);

        mockMvc.perform(delete("/api/asignaciones/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteAsignacion_noExistente() throws Exception {
        Mockito.when(asignacionService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/asignaciones/99"))
                .andExpect(status().isNotFound());
    }
}
