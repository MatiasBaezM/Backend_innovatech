package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.RechazoRequest;
import Innovatech.ms_gestion_proyectos.model.Tarea;
import Innovatech.ms_gestion_proyectos.model.Tarea.Estado;
import Innovatech.ms_gestion_proyectos.model.Tarea.Prioridad;
import Innovatech.ms_gestion_proyectos.security.AuthenticatedUser;
import Innovatech.ms_gestion_proyectos.security.JwtUtil;
import Innovatech.ms_gestion_proyectos.service.TareaService;
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

@WebMvcTest(value = TareaController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class TareaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TareaService tareaService;

    @MockBean
    private JwtUtil jwtUtil; // requerido por JwtAuthenticationFilter (@Component)

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

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

    private Tarea buildTarea(Long id, Long proyectoId, Estado estado) {
        return Tarea.builder()
                .id(id).proyectoId(proyectoId)
                .titulo("Tarea Test").descripcion("Descripción")
                .prioridad(Prioridad.ALTA).estado(estado)
                .asignadoId(10L).asignadoNombre("Juan")
                .fechaCreacion(LocalDate.of(2025, 1, 10))
                .fechaLimite(LocalDate.of(2025, 2, 10))
                .build();
    }

    // ── GET /api/proyectos/{id}/tareas ────────────────────────────────────────

    @Test
    void testGetTareas() throws Exception {
        Mockito.when(tareaService.getTareasByProyecto(5L))
                .thenReturn(Arrays.asList(
                        buildTarea(1L, 5L, Estado.POR_HACER),
                        buildTarea(2L, 5L, Estado.EN_PROGRESO)
                ));

        mockMvc.perform(get("/api/proyectos/5/tareas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].titulo", is("Tarea Test")))
                .andExpect(jsonPath("$[1].estado", is("EN_PROGRESO")));
    }

    // ── GET /api/proyectos/{id}/tareas/{tid} ──────────────────────────────────

    @Test
    void testGetTareaById_existente() throws Exception {
        Tarea tarea = buildTarea(1L, 5L, Estado.POR_HACER);
        Mockito.when(tareaService.getTareaById(1L)).thenReturn(Optional.of(tarea));

        mockMvc.perform(get("/api/proyectos/5/tareas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Tarea Test"));
    }

    @Test
    void testGetTareaById_noExistente() throws Exception {
        Mockito.when(tareaService.getTareaById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/proyectos/5/tareas/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/proyectos/{id}/tareas ───────────────────────────────────────

    @Test
    void testCreateTarea_comoAdministrador() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Tarea nueva = buildTarea(null, 5L, Estado.POR_HACER);
        Tarea guardada = buildTarea(1L, 5L, Estado.POR_HACER);

        Mockito.when(tareaService.createTarea(eq(5L), any(Tarea.class), eq("ADMINISTRADOR"), eq(1L)))
                .thenReturn(guardada);

        mockMvc.perform(post("/api/proyectos/5/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("POR_HACER"));
    }

    @Test
    void testCreateTarea_comoColaborador_retorna403() throws Exception {
        setAuthentication("COLABORADOR", 5L);
        Tarea nueva = buildTarea(null, 5L, Estado.POR_HACER);

        mockMvc.perform(post("/api/proyectos/5/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateTarea_gestorProyectoAjeno_retorna403() throws Exception {
        setAuthentication("GESTOR_PROYECTOS", 99L);
        Tarea nueva = buildTarea(null, 5L, Estado.POR_HACER);
        Mockito.when(tareaService.createTarea(eq(5L), any(Tarea.class), eq("GESTOR_PROYECTOS"), eq(99L)))
                .thenThrow(new SecurityException("No autorizado"));

        mockMvc.perform(post("/api/proyectos/5/tareas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/proyectos/{id}/tareas/{tid} ──────────────────────────────────

    @Test
    void testUpdateTarea_exitoso() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Tarea actualizada = buildTarea(1L, 5L, Estado.EN_PROGRESO);
        Mockito.when(tareaService.updateTarea(eq(1L), any(Tarea.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/proyectos/5/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROGRESO"));
    }

    @Test
    void testUpdateTarea_colaboradorIntentaRevisar_retorna403() throws Exception {
        setAuthentication("COLABORADOR", 5L);
        Tarea tarea = buildTarea(1L, 5L, Estado.REVISADO);

        mockMvc.perform(put("/api/proyectos/5/tareas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarea)))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/proyectos/{id}/tareas/{tid}/aprobar ────────────────────────

    @Test
    void testAprobarTarea_comoAdministrador() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Tarea aprobada = buildTarea(1L, 5L, Estado.REVISADO);
        Mockito.when(tareaService.aprobarTarea(1L)).thenReturn(aprobada);

        mockMvc.perform(patch("/api/proyectos/5/tareas/1/aprobar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REVISADO"));
    }

    @Test
    void testAprobarTarea_comoGestor_retorna403() throws Exception {
        setAuthentication("GESTOR_PROYECTOS", 2L);

        mockMvc.perform(patch("/api/proyectos/5/tareas/1/aprobar"))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/proyectos/{id}/tareas/{tid}/rechazar ───────────────────────

    @Test
    void testRechazarTarea_comoAdministrador() throws Exception {
        setAuthentication("ADMINISTRADOR", 1L);
        Tarea rechazada = buildTarea(1L, 5L, Estado.POR_HACER);
        rechazada.setMensajeCorreccion("Falta documentación");

        RechazoRequest rechazoRequest = new RechazoRequest();
        rechazoRequest.setMensajeCorreccion("Falta documentación");

        Mockito.when(tareaService.rechazarTarea(eq(1L), eq("Falta documentación")))
                .thenReturn(rechazada);

        mockMvc.perform(patch("/api/proyectos/5/tareas/1/rechazar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rechazoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("POR_HACER"))
                .andExpect(jsonPath("$.mensajeCorreccion").value("Falta documentación"));
    }

    @Test
    void testRechazarTarea_comoColaborador_retorna403() throws Exception {
        setAuthentication("COLABORADOR", 5L);
        RechazoRequest rechazoRequest = new RechazoRequest();
        rechazoRequest.setMensajeCorreccion("Mensaje");

        mockMvc.perform(patch("/api/proyectos/5/tareas/1/rechazar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rechazoRequest)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/proyectos/{id}/tareas/{tid} ───────────────────────────────

    @Test
    void testDeleteTarea() throws Exception {
        Mockito.doNothing().when(tareaService).deleteTarea(1L);

        mockMvc.perform(delete("/api/proyectos/5/tareas/1"))
                .andExpect(status().isNoContent());
    }
}
