package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.Actividad;
import Innovatech.ms_gestion_proyectos.repository.ActividadRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActividadController.class)
@ActiveProfiles("test")
class ActividadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActividadRepository actividadRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Actividad buildActividad(Long id, String titulo) {
        return Actividad.builder()
                .id(id).titulo(titulo)
                .fechaCreacion(LocalDateTime.of(2025, 1, 10, 12, 0))
                .build();
    }

    @Test
    void testGetRecentActivities() throws Exception {
        Mockito.when(actividadRepository.findTop10ByOrderByFechaCreacionDesc()).thenReturn(Arrays.asList(
                buildActividad(1L, "Tarea creada"),
                buildActividad(2L, "Proyecto actualizado")
        ));

        mockMvc.perform(get("/api/proyectos/actividades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo", is("Tarea creada")));
    }

    @Test
    void testCreateActividad() throws Exception {
        Actividad nueva = buildActividad(null, "Nueva actividad");
        Mockito.when(actividadRepository.save(any(Actividad.class))).thenReturn(buildActividad(1L, "Nueva actividad"));

        mockMvc.perform(post("/api/proyectos/actividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Nueva actividad"));
    }
}
