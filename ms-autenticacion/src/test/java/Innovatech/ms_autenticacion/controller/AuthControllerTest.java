package Innovatech.ms_autenticacion.controller;

import Innovatech.ms_autenticacion.model.AuthRequest;
import Innovatech.ms_autenticacion.model.AuthResponse;
import Innovatech.ms_autenticacion.model.Usuario;
import Innovatech.ms_autenticacion.security.JwtUtil;
import Innovatech.ms_autenticacion.service.AuthService;
import io.jsonwebtoken.JwtException;
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

import Innovatech.ms_autenticacion.model.HabilidadIdsRequest;
import Innovatech.ms_autenticacion.model.HabilidadInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Usuario buildUsuario(Long id, String rut, String nombre, String rol) {
        return Usuario.builder()
                .id(id).rut(rut).nombre(nombre).clave("$hash")
                .rol(rol).correo(rut + "@test.cl")
                .habilidadIds(new HashSet<>()).build();
    }

    // ── POST /auth/login ──────────────────────────────────────────────────────

    @Test
    void testLogin_exitoso() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setRut("12.345.678-9");
        request.setClave("clave123");

        Mockito.when(authService.login(any(AuthRequest.class)))
                .thenReturn(new AuthResponse("jwt-token-generado"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-generado"));
    }

    @Test
    void testLogin_credencialesInvalidas_retorna401() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setRut("99.999.999-9");
        request.setClave("wrong");

        Mockito.when(authService.login(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Credenciales inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /auth/users ───────────────────────────────────────────────────────

    @Test
    void testGetAllUsers_sinFiltro() throws Exception {
        List<Usuario> usuarios = List.of(
                buildUsuario(1L, "11.111.111-1", "Juan", "COLABORADOR"),
                buildUsuario(2L, "22.222.222-2", "Maria", "GESTOR_PROYECTOS")
        );
        Mockito.when(authService.getAllUsers(null)).thenReturn(usuarios);

        mockMvc.perform(get("/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Juan")))
                .andExpect(jsonPath("$[1].rol", is("GESTOR_PROYECTOS")));
    }

    @Test
    void testGetAllUsers_conFiltroRol() throws Exception {
        List<Usuario> colaboradores = List.of(
                buildUsuario(1L, "11.111.111-1", "Juan", "COLABORADOR")
        );
        Mockito.when(authService.getAllUsers("COLABORADOR")).thenReturn(colaboradores);

        mockMvc.perform(get("/auth/users").param("rol", "COLABORADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rol", is("COLABORADOR")));
    }

    // ── GET /auth/users/{id} ──────────────────────────────────────────────────

    @Test
    void testGetUserById_existente() throws Exception {
        Usuario usuario = buildUsuario(1L, "11.111.111-1", "Juan", "COLABORADOR");
        Mockito.when(authService.getUserById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void testGetUserById_noEncontrado() throws Exception {
        Mockito.when(authService.getUserById(99L))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/auth/users/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /auth/register ───────────────────────────────────────────────────

    @Test
    void testRegister_exitoso() throws Exception {
        Usuario nuevo = buildUsuario(null, "33.333.333-3", "Carlos", "COLABORADOR");
        nuevo.setClave("clave123");
        Usuario guardado = buildUsuario(3L, "33.333.333-3", "Carlos", "COLABORADOR");

        Mockito.when(authService.register(any(Usuario.class))).thenReturn(guardado);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    // ── PUT /auth/users/{id} ──────────────────────────────────────────────────

    @Test
    void testUpdateUser_exitoso() throws Exception {
        Usuario datos = buildUsuario(1L, "11.111.111-1", "Juan Actualizado", "GESTOR_PROYECTOS");
        Mockito.when(authService.updateUser(eq(1L), any(Usuario.class))).thenReturn(datos);

        mockMvc.perform(put("/auth/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Actualizado"))
                .andExpect(jsonPath("$.rol").value("GESTOR_PROYECTOS"));
    }

    @Test
    void testUpdateUser_noEncontrado() throws Exception {
        Usuario datos = buildUsuario(null, "99.999.999-9", "Ghost", "COLABORADOR");
        Mockito.when(authService.updateUser(eq(99L), any(Usuario.class)))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(put("/auth/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /auth/users/{id} ───────────────────────────────────────────────

    @Test
    void testDeleteUser() throws Exception {
        Mockito.doNothing().when(authService).deleteUser(1L);

        mockMvc.perform(delete("/auth/users/1"))
                .andExpect(status().isOk());
    }

    // ── GET /auth/me ──────────────────────────────────────────────────────────

    @Test
    void testGetMe_tokenValido() throws Exception {
        Usuario usuario = buildUsuario(1L, "12.345.678-9", "Admin", "ADMINISTRADOR");
        Mockito.when(jwtUtil.extractRut("mi-token")).thenReturn("12.345.678-9");
        Mockito.when(authService.getMe("12.345.678-9")).thenReturn(usuario);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer mi-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Admin"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }

    @Test
    void testGetMe_tokenInvalido_retorna401() throws Exception {
        Mockito.when(jwtUtil.extractRut("token-invalido"))
                .thenThrow(new JwtException("Token inválido"));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /auth/users/{id}/habilidades ──────────────────────────────────────

    @Test
    void testGetHabilidadesUsuario() throws Exception {
        Mockito.when(authService.getHabilidadesByUsuario(1L)).thenReturn(List.of(
                new HabilidadInfo(5L, "Desarrollador Backend", "#6366f1"),
                new HabilidadInfo(7L, "DevOps", "#ef4444")
        ));

        mockMvc.perform(get("/auth/users/1/habilidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre", is("Desarrollador Backend")));
    }

    @Test
    void testGetHabilidadesUsuario_noEncontrado_retorna404() throws Exception {
        Mockito.when(authService.getHabilidadesByUsuario(99L))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/auth/users/99/habilidades"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /auth/users/{id}/habilidades ──────────────────────────────────────

    @Test
    void testUpdateHabilidadesUsuario() throws Exception {
        HabilidadIdsRequest request = new HabilidadIdsRequest(Set.of(5L));
        Mockito.when(authService.updateHabilidadesForUsuario(eq(1L), any()))
                .thenReturn(List.of(new HabilidadInfo(5L, "Desarrollador Backend", "#6366f1")));

        mockMvc.perform(put("/auth/users/1/habilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Desarrollador Backend")));
    }

    @Test
    void testUpdateHabilidadesUsuario_noEncontrado_retorna404() throws Exception {
        HabilidadIdsRequest request = new HabilidadIdsRequest(Set.of(5L));
        Mockito.when(authService.updateHabilidadesForUsuario(eq(99L), any()))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(put("/auth/users/99/habilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
