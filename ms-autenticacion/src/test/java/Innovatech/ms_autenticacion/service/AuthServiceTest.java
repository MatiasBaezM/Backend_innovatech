package Innovatech.ms_autenticacion.service;

import Innovatech.ms_autenticacion.model.AuthRequest;
import Innovatech.ms_autenticacion.model.AuthResponse;
import Innovatech.ms_autenticacion.model.HabilidadInfo;
import Innovatech.ms_autenticacion.model.Usuario;
import Innovatech.ms_autenticacion.repository.UsuarioRepository;
import Innovatech.ms_autenticacion.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "entityManager", entityManager);
    }

    private Usuario buildUsuario(Long id, String rut, String nombre, String clave, String rol) {
        return Usuario.builder()
                .id(id).rut(rut).nombre(nombre).clave(clave)
                .rol(rol).correo(nombre.toLowerCase().replace(" ", "") + "@test.cl")
                .habilidadIds(new HashSet<>()).build();
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void testLogin_credencialesValidas() {
        Usuario usuario = buildUsuario(1L, "12.345.678-9", "Admin", "$2a$10$hashSeguro", "ADMINISTRADOR");
        AuthRequest request = new AuthRequest();
        request.setRut("12.345.678-9");
        request.setClave("clave123");

        when(usuarioRepository.findByRut("12.345.678-9")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "$2a$10$hashSeguro")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "12.345.678-9", "ADMINISTRADOR", "Admin")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(usuarioRepository, times(1)).findByRut("12.345.678-9");
        verify(jwtUtil, times(1)).generateToken(1L, "12.345.678-9", "ADMINISTRADOR", "Admin");
    }

    @Test
    void testLogin_credencialesInvalidas_usuarioNoExiste() {
        AuthRequest request = new AuthRequest();
        request.setRut("99.999.999-9");
        request.setClave("wrongpass");

        when(usuarioRepository.findByRut("99.999.999-9")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
        verify(jwtUtil, never()).generateToken(any(), any(), any(), any());
    }

    @Test
    void testLogin_claveIncorrecta() {
        Usuario usuario = buildUsuario(1L, "12.345.678-9", "Admin", "$2a$10$hashSeguro", "ADMINISTRADOR");
        AuthRequest request = new AuthRequest();
        request.setRut("12.345.678-9");
        request.setClave("claveErronea");

        when(usuarioRepository.findByRut("12.345.678-9")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("claveErronea", "$2a$10$hashSeguro")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void testRegister_usuarioNuevo() {
        Usuario nuevo = buildUsuario(null, "11.111.111-1", "Juan", "clave123", "COLABORADOR");
        Usuario guardado = buildUsuario(1L, "11.111.111-1", "Juan", "$2a$10$hash", "COLABORADOR");

        when(usuarioRepository.findByRut("11.111.111-1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("$2a$10$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);

        Usuario resultado = authService.register(nuevo);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("COLABORADOR", resultado.getRol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testRegister_rutDuplicado() {
        Usuario existente = buildUsuario(1L, "11.111.111-1", "Juan", "$2a$10$hash", "COLABORADOR");
        when(usuarioRepository.findByRut("11.111.111-1")).thenReturn(Optional.of(existente));

        Usuario nuevo = buildUsuario(null, "11.111.111-1", "Juan2", "clave", "COLABORADOR");

        assertThrows(RuntimeException.class, () -> authService.register(nuevo));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testRegister_sinClave_lanzaExcepcion() {
        Usuario nuevo = buildUsuario(null, "22.222.222-2", "Maria", "", "COLABORADOR");
        when(usuarioRepository.findByRut("22.222.222-2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.register(nuevo));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testRegister_rolInvalido_lanzaExcepcion() {
        Usuario nuevo = buildUsuario(null, "33.333.333-3", "Carlos", "clave123", "ANALISTA");
        when(usuarioRepository.findByRut("33.333.333-3")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.register(nuevo));
    }

    // ── getAllUsers ────────────────────────────────────────────────────────────

    @Test
    void testGetAllUsers_sinFiltro() {
        List<Usuario> usuarios = List.of(
                buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR"),
                buildUsuario(2L, "22.222.222-2", "Maria", "$hash", "GESTOR_PROYECTOS")
        );
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<Usuario> resultado = authService.getAllUsers(null);

        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
        verify(usuarioRepository, never()).findByRol(anyString());
    }

    @Test
    void testGetAllUsers_conFiltroRol() {
        List<Usuario> gestores = List.of(
                buildUsuario(2L, "22.222.222-2", "Maria", "$hash", "GESTOR_PROYECTOS")
        );
        when(usuarioRepository.findByRol("GESTOR_PROYECTOS")).thenReturn(gestores);

        List<Usuario> resultado = authService.getAllUsers("GESTOR_PROYECTOS");

        assertEquals(1, resultado.size());
        assertEquals("GESTOR_PROYECTOS", resultado.get(0).getRol());
        verify(usuarioRepository, times(1)).findByRol("GESTOR_PROYECTOS");
        verify(usuarioRepository, never()).findAll();
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void testUpdateUser_adminCambiaRol_exitoso() {
        Usuario existente = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        Usuario datos = buildUsuario(null, "11.111.111-1", "Juan Actualizado", null, "GESTOR_PROYECTOS");
        Usuario actualizado = buildUsuario(1L, "11.111.111-1", "Juan Actualizado", "$hash", "GESTOR_PROYECTOS");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(actualizado);

        // caller = ADMINISTRADOR (id 99) editando a otro usuario
        Usuario resultado = authService.updateUser(1L, datos, 99L, "ADMINISTRADOR");

        assertNotNull(resultado);
        assertEquals("Juan Actualizado", resultado.getNombre());
        assertEquals("GESTOR_PROYECTOS", resultado.getRol());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testUpdateUser_cambioClavePropia_soloClave_noBorraOtrosCampos() {
        Usuario existente = buildUsuario(1L, "11.111.111-1", "Juan", "$hashViejo", "COLABORADOR");
        // El frontend de cambio de clave envia SOLO la clave
        Usuario datos = new Usuario();
        datos.setClave("nuevaClave");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nuevaClave")).thenReturn("$hashNuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // caller = el mismo COLABORADOR (id 1) cambiando su propia clave
        Usuario resultado = authService.updateUser(1L, datos, 1L, "COLABORADOR");

        assertEquals("Juan", resultado.getNombre());          // intacto
        assertEquals("11.111.111-1", resultado.getRut());     // intacto
        assertEquals("COLABORADOR", resultado.getRol());      // intacto
        assertEquals("$hashNuevo", resultado.getClave());     // actualizado
    }

    @Test
    void testUpdateUser_noAdminEditaOtroUsuario_lanzaSecurityException() {
        Usuario datos = new Usuario();
        datos.setClave("hack");

        // caller = COLABORADOR (id 5) intentando editar al usuario 1
        assertThrows(SecurityException.class,
                () -> authService.updateUser(1L, datos, 5L, "COLABORADOR"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_noAdminIntentaCambiarRol_lanzaSecurityException() {
        Usuario existente = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        Usuario datos = buildUsuario(null, null, null, null, "ADMINISTRADOR"); // auto-promocion

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        // caller = el propio COLABORADOR (id 1) intentando subirse a ADMINISTRADOR
        assertThrows(SecurityException.class,
                () -> authService.updateUser(1L, datos, 1L, "COLABORADOR"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_rolInvalido_lanzaExcepcion() {
        Usuario existente = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        Usuario datos = buildUsuario(null, "11.111.111-1", "Juan", null, "ANALISTA");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(RuntimeException.class,
                () -> authService.updateUser(1L, datos, 99L, "ADMINISTRADOR"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testUpdateUser_usuarioNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        Usuario datos = buildUsuario(null, "11.111.111-1", "Juan", null, "COLABORADOR");

        assertThrows(RuntimeException.class,
                () -> authService.updateUser(99L, datos, 99L, "ADMINISTRADOR"));
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void testDeleteUser() {
        doNothing().when(usuarioRepository).deleteById(1L);

        authService.deleteUser(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void testGetUserById_existente() {
        Usuario usuario = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = authService.getUserById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_noEncontrado_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getUserById(99L));
    }

    // ── getHabilidadesByUsuario ────────────────────────────────────────────────

    @Test
    void testGetHabilidadesByUsuario_conHabilidades() {
        Usuario usuario = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        usuario.setHabilidadIds(new HashSet<>(Set.of(5L)));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("ids"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(
                new Object[]{5L, "Desarrollador Backend", "#6366f1"}));

        List<HabilidadInfo> resultado = authService.getHabilidadesByUsuario(1L);

        assertEquals(1, resultado.size());
        assertEquals("Desarrollador Backend", resultado.get(0).getNombre());
        assertEquals("#6366f1", resultado.get(0).getColor());
    }

    @Test
    void testGetHabilidadesByUsuario_sinHabilidades() {
        Usuario usuario = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        usuario.setHabilidadIds(new HashSet<>());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        List<HabilidadInfo> resultado = authService.getHabilidadesByUsuario(1L);

        assertTrue(resultado.isEmpty());
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    @Test
    void testGetHabilidadesByUsuario_usuarioNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getHabilidadesByUsuario(99L));
    }

    // ── updateHabilidadesForUsuario ────────────────────────────────────────────

    @Test
    void testUpdateHabilidadesForUsuario_conIds() {
        Usuario usuario = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(eq("ids"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(
                new Object[]{7L, "DevOps", "#ef4444"}));

        List<HabilidadInfo> resultado = authService.updateHabilidadesForUsuario(1L, Set.of(7L));

        assertEquals(1, resultado.size());
        assertEquals("DevOps", resultado.get(0).getNombre());
        assertTrue(usuario.getHabilidadIds().contains(7L));
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testUpdateHabilidadesForUsuario_idsNull() {
        Usuario usuario = buildUsuario(1L, "11.111.111-1", "Juan", "$hash", "COLABORADOR");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        List<HabilidadInfo> resultado = authService.updateHabilidadesForUsuario(1L, null);

        assertTrue(resultado.isEmpty());
        assertTrue(usuario.getHabilidadIds().isEmpty());
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    @Test
    void testUpdateHabilidadesForUsuario_usuarioNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.updateHabilidadesForUsuario(99L, Set.of(1L)));
    }
}
