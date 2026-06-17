package Innovatech.ms_recursos_colaboraciones.controller;

import Innovatech.ms_recursos_colaboraciones.model.Habilidad;
import Innovatech.ms_recursos_colaboraciones.security.JwtUtil;
import Innovatech.ms_recursos_colaboraciones.service.HabilidadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
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

@WebMvcTest(value = HabilidadController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class HabilidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabilidadService habilidadService;

    @MockBean
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Habilidad buildHabilidad(Long id, String nombre, String color) {
        return Habilidad.builder().id(id).nombre(nombre).color(color).build();
    }

    // ── GET /api/habilidades ──────────────────────────────────────────────────

    @Test
    void testGetAllHabilidades() throws Exception {
        Mockito.when(habilidadService.findAll()).thenReturn(Arrays.asList(
                buildHabilidad(1L, "Desarrollador Backend", "#6366f1"),
                buildHabilidad(2L, "Desarrollador Frontend", "#0ea5e9"),
                buildHabilidad(3L, "DBA", "#10b981")
        ));

        mockMvc.perform(get("/api/habilidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nombre", is("Desarrollador Backend")))
                .andExpect(jsonPath("$[0].color", is("#6366f1")))
                .andExpect(jsonPath("$[2].nombre", is("DBA")));
    }

    // ── GET /api/habilidades/{id} ─────────────────────────────────────────────

    @Test
    void testGetHabilidadById_existente() throws Exception {
        Habilidad habilidad = buildHabilidad(1L, "DevOps", "#ef4444");
        Mockito.when(habilidadService.findById(1L)).thenReturn(Optional.of(habilidad));

        mockMvc.perform(get("/api/habilidades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("DevOps"))
                .andExpect(jsonPath("$.color").value("#ef4444"));
    }

    @Test
    void testGetHabilidadById_noExistente() throws Exception {
        Mockito.when(habilidadService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/habilidades/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/habilidades ─────────────────────────────────────────────────

    @Test
    void testCreateHabilidad() throws Exception {
        Habilidad nueva = buildHabilidad(null, "Analista QA", "#f59e0b");
        Habilidad guardada = buildHabilidad(4L, "Analista QA", "#f59e0b");

        Mockito.when(habilidadService.save(any(Habilidad.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/habilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.nombre").value("Analista QA"))
                .andExpect(jsonPath("$.color").value("#f59e0b"));
    }

    // ── PUT /api/habilidades/{id} ─────────────────────────────────────────────

    @Test
    void testUpdateHabilidad_existente() throws Exception {
        Habilidad existente = buildHabilidad(1L, "Backend Viejo", "#000000");
        Habilidad actualizada = buildHabilidad(1L, "Desarrollador Backend", "#6366f1");

        Mockito.when(habilidadService.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(habilidadService.save(any(Habilidad.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/habilidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Desarrollador Backend"))
                .andExpect(jsonPath("$.color").value("#6366f1"));
    }

    @Test
    void testUpdateHabilidad_noExistente() throws Exception {
        Habilidad datos = buildHabilidad(null, "Ghost", "#ffffff");
        Mockito.when(habilidadService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/habilidades/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/habilidades/{id} ──────────────────────────────────────────

    @Test
    void testDeleteHabilidad_existente() throws Exception {
        Habilidad existente = buildHabilidad(1L, "Scrum Master", "#8b5cf6");
        Mockito.when(habilidadService.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.doNothing().when(habilidadService).deleteById(1L);

        mockMvc.perform(delete("/api/habilidades/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteHabilidad_noExistente() throws Exception {
        Mockito.when(habilidadService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/habilidades/99"))
                .andExpect(status().isNotFound());
    }
}
