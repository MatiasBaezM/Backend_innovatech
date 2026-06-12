package Innovatech.ms_autenticacion.service;

import Innovatech.ms_autenticacion.model.AuthRequest;
import Innovatech.ms_autenticacion.model.AuthResponse;
import Innovatech.ms_autenticacion.model.HabilidadInfo;
import Innovatech.ms_autenticacion.model.Usuario;
import Innovatech.ms_autenticacion.repository.UsuarioRepository;
import Innovatech.ms_autenticacion.security.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> ROLES_VALIDOS = Set.of("ADMINISTRADOR", "GESTOR_PROYECTOS", "COLABORADOR");

    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public AuthResponse login(AuthRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByRut(request.getRut())
                .filter(u -> passwordMatches(request.getClave(), u));

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String token = jwtUtil.generateToken(usuario.getId(), usuario.getRut(), usuario.getRol(), usuario.getNombre());
            return new AuthResponse(token);
        } else {
            throw new RuntimeException("Credenciales inválidas: RUT o Clave incorrectos");
        }
    }

    /**
     * Compara la clave contra el hash BCrypt almacenado. Si la clave guardada
     * es legada (texto plano, anterior al hashing) y coincide, se re-hashea
     * al vuelo para migrarla sin invalidar las cuentas existentes.
     */
    private boolean passwordMatches(String rawClave, Usuario usuario) {
        String stored = usuario.getClave();
        if (stored == null || rawClave == null) {
            return false;
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(rawClave, stored);
        }
        if (stored.equals(rawClave)) {
            usuario.setClave(passwordEncoder.encode(rawClave));
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    public Usuario register(Usuario usuario) {
        if (usuarioRepository.findByRut(usuario.getRut()).isPresent()) {
            throw new RuntimeException("El usuario con este RUT ya existe");
        }
        if (usuario.getClave() == null || usuario.getClave().isBlank()) {
            throw new RuntimeException("La clave es obligatoria");
        }
        usuario.setClave(passwordEncoder.encode(usuario.getClave()));
        usuario.setRol(resolveRolForCreate(usuario.getRol()));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> getAllUsers(String rol) {
        if (rol != null && !rol.isBlank()) {
            return usuarioRepository.findByRol(rol);
        }
        return usuarioRepository.findAll();
    }

    public void deleteUser(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario getMe(String rut) {
        Usuario usuario = usuarioRepository.findByRut(rut)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        populateHabilidades(usuario);
        return usuario;
    }

    public Usuario getUserById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        populateHabilidades(usuario);
        return usuario;
    }

    public Usuario updateUser(Long id, Usuario userDetails) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setNombre(userDetails.getNombre());
        user.setRut(userDetails.getRut());
        user.setCorreo(userDetails.getCorreo());
        if (userDetails.getRol() != null && !userDetails.getRol().isBlank()) {
            if (!ROLES_VALIDOS.contains(userDetails.getRol())) {
                throw new RuntimeException("Rol inválido: " + userDetails.getRol());
            }
            user.setRol(userDetails.getRol());
        }
        if (userDetails.getClave() != null && !userDetails.getClave().isEmpty()) {
            user.setClave(passwordEncoder.encode(userDetails.getClave()));
        }
        if (userDetails.getHabilidadIds() != null) {
            user.setHabilidadIds(new HashSet<>(userDetails.getHabilidadIds()));
        }

        return usuarioRepository.save(user);
    }

    public List<HabilidadInfo> getHabilidadesByUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        populateHabilidades(usuario);
        return usuario.getHabilidades();
    }

    public List<HabilidadInfo> updateHabilidadesForUsuario(Long id, Set<Long> habilidadIds) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setHabilidadIds(habilidadIds != null ? new HashSet<>(habilidadIds) : new HashSet<>());
        usuarioRepository.save(usuario);
        populateHabilidades(usuario);
        return usuario.getHabilidades();
    }

    private String resolveRolForCreate(String rol) {
        if (rol == null || rol.isBlank()) {
            return "COLABORADOR";
        }
        if (!ROLES_VALIDOS.contains(rol)) {
            throw new RuntimeException("Rol inválido: " + rol);
        }
        return rol;
    }

    @SuppressWarnings("unchecked")
    private void populateHabilidades(Usuario usuario) {
        if (usuario.getHabilidadIds() == null || usuario.getHabilidadIds().isEmpty()) {
            usuario.setHabilidades(List.of());
            return;
        }
        List<Object[]> rows = entityManager.createNativeQuery(
                        "SELECT id, nombre, color FROM db_recursos.habilidades WHERE id IN (:ids)")
                .setParameter("ids", usuario.getHabilidadIds())
                .getResultList();
        usuario.setHabilidades(rows.stream()
                .map(r -> new HabilidadInfo(((Number) r[0]).longValue(), (String) r[1], (String) r[2]))
                .toList());
    }
}
