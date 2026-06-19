package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.Colaborador;
import Innovatech.ms_gestion_proyectos.model.EquipoRequest;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.security.AuthenticatedUser;
import Innovatech.ms_gestion_proyectos.security.JwtUtil;
import Innovatech.ms_gestion_proyectos.service.ProyectoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProyectoController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class ProyectoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProyectoService proyectoService;

    @MockBean
    private JwtUtil jwtUtil; // requerido por JwtAuthenticationFilter (@Component)

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private void setAuthentication(String rol, Long userId) {
        var auth = new UsernamePasswordAuthenticationToken("test-rut", null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
        auth.setDetails(new AuthenticatedUser(rol, userId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Proyecto buildProyecto(Long id, String nombre, Long gestorId) {
        return Proyecto.builder()
                .id(id).nombre(nombre).descripcion("Desc").estado("EN_PROGRESO")
                .fechaInicio(LocalDate.of(2025, 1, 1)).gestorId(gestorId).gestorNombre("Gestor")
                .build();
    }

    @Test
    void testGetAllProyectos() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Mockito.when(proyectoService.getAllProyectos("ADMINISTRADOR", 1L)).thenReturn(Arrays.asList(
                buildProyecto(1L, "Proyecto A", 10L),
                buildProyecto(2L, "Proyecto B", 20L)
        ));

        mockMvc.perform(get("/api/proyectos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Proyecto A")));
    }

    @Test
    void testGetProyectoById_existente() throws Exception {
        Mockito.when(proyectoService.getProyectoById(1L)).thenReturn(Optional.of(buildProyecto(1L, "Proyecto A", 10L)));

        mockMvc.perform(get("/api/proyectos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proyecto A"));
    }

    @Test
    void testGetProyectoById_noExistente() throws Exception {
        Mockito.when(proyectoService.getProyectoById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/proyectos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateProyecto_comoAdministrador() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Proyecto nuevo = buildProyecto(null, "Nuevo Proyecto", 10L);
        Mockito.when(proyectoService.createProyecto(any(Proyecto.class))).thenReturn(buildProyecto(1L, "Nuevo Proyecto", 10L));

        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nuevo Proyecto"));
    }

    @Test
    void testCreateProyecto_comoColaborador_retorna403() throws Exception {
        setAuthentication("COLABORADOR", 5L);
        Proyecto nuevo = buildProyecto(null, "Nuevo Proyecto", 10L);

        mockMvc.perform(post("/api/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateProyecto_existente() throws Exception {
        Proyecto datos = buildProyecto(null, "Actualizado", 10L);
        Mockito.when(proyectoService.updateProyecto(eq(1L), any(Proyecto.class))).thenReturn(buildProyecto(1L, "Actualizado", 10L));

        mockMvc.perform(put("/api/proyectos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado"));
    }

    @Test
    void testUpdateProyecto_noExistente() throws Exception {
        Proyecto datos = buildProyecto(null, "Ghost", 10L);
        Mockito.when(proyectoService.updateProyecto(eq(99L), any(Proyecto.class)))
                .thenThrow(new RuntimeException("Proyecto no encontrado"));

        mockMvc.perform(put("/api/proyectos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateEquipo_comoAdmin() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        EquipoRequest request = new EquipoRequest(5L, "Gestor Nuevo", List.of(new Colaborador(10L, "Colab")));
        Mockito.when(proyectoService.updateEquipo(eq(1L), any(EquipoRequest.class))).thenReturn(buildProyecto(1L, "Proyecto A", 5L));

        mockMvc.perform(put("/api/proyectos/1/equipo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateEquipo_sinPermisos_retorna403() throws Exception {
        setAuthentication("COLABORADOR", 5L);
        EquipoRequest request = new EquipoRequest(5L, "Gestor Nuevo", List.of());

        mockMvc.perform(put("/api/proyectos/1/equipo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateEquipo_proyectoNoExistente_retorna404() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        EquipoRequest request = new EquipoRequest(5L, "Gestor Nuevo", List.of());
        Mockito.when(proyectoService.updateEquipo(eq(99L), any(EquipoRequest.class)))
                .thenThrow(new RuntimeException("Proyecto no encontrado"));

        mockMvc.perform(put("/api/proyectos/99/equipo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProyecto_comoAdministrador() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Mockito.doNothing().when(proyectoService).deleteProyecto(1L);

        mockMvc.perform(delete("/api/proyectos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteProyecto_comoColaborador_retorna403() throws Exception {
        setAuthentication("COLABORADOR", 5L);

        mockMvc.perform(delete("/api/proyectos/1"))
                .andExpect(status().isForbidden());
    }
}
