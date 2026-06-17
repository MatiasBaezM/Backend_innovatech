package Innovatech.ms_analiticas.controller;

import Innovatech.ms_analiticas.dto.CargaTrabajoDTO;
import Innovatech.ms_analiticas.dto.CostoProyectoDTO;
import Innovatech.ms_analiticas.dto.GrupoConteoDTO;
import Innovatech.ms_analiticas.dto.ResumenDTO;
import Innovatech.ms_analiticas.security.JwtUtil;
import Innovatech.ms_analiticas.service.AnaliticasService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AnaliticasController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class AnaliticasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnaliticasService analiticasService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testGetResumen() throws Exception {
        ResumenDTO resumen = new ResumenDTO();
        resumen.setTotalProyectos(10L);
        resumen.setProyectosActivos(4L);
        resumen.setTotalTrabajadores(8L);
        resumen.setTareasPendientes(15L);
        resumen.setTareasCompletadas(30L);
        resumen.setPresupuestoTotal(5000.0);
        Mockito.when(analiticasService.getResumen()).thenReturn(resumen);

        mockMvc.perform(get("/api/analiticas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProyectos").value(10L))
                .andExpect(jsonPath("$.presupuestoTotal").value(5000.0));
    }

    @Test
    void testGetProyectosPorEstado() throws Exception {
        Mockito.when(analiticasService.getProyectosPorEstado()).thenReturn(Arrays.asList(
                new GrupoConteoDTO("INICIO", 2L),
                new GrupoConteoDTO("EN_PROGRESO", 5L)
        ));

        mockMvc.perform(get("/api/analiticas/proyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].label", is("INICIO")));
    }

    @Test
    void testGetTareasPorPrioridad() throws Exception {
        Mockito.when(analiticasService.getTareasPorPrioridad()).thenReturn(Arrays.asList(
                new GrupoConteoDTO("ALTA", 10L),
                new GrupoConteoDTO("MEDIA", 25L)
        ));

        mockMvc.perform(get("/api/analiticas/tareas/prioridad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].label", is("MEDIA")));
    }

    @Test
    void testGetTareasPorEstado() throws Exception {
        Mockito.when(analiticasService.getTareasPorEstado()).thenReturn(Arrays.asList(
                new GrupoConteoDTO("POR_HACER", 5L),
                new GrupoConteoDTO("REVISADO", 20L)
        ));

        mockMvc.perform(get("/api/analiticas/tareas/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetCargaTrabajo() throws Exception {
        Mockito.when(analiticasService.getCargaTrabajo()).thenReturn(Arrays.asList(
                new CargaTrabajoDTO("Juan Pérez", 40L),
                new CargaTrabajoDTO("María Silva", 32L)
        ));

        mockMvc.perform(get("/api/analiticas/carga-trabajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Juan Pérez")));
    }

    @Test
    void testGetCostosProyectos() throws Exception {
        Mockito.when(analiticasService.getCostosProyectos()).thenReturn(Arrays.asList(
                new CostoProyectoDTO(1L, 12500.0),
                new CostoProyectoDTO(2L, 8000.0)
        ));

        mockMvc.perform(get("/api/analiticas/costos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].costoEstimado").value(12500.0));
    }
}
