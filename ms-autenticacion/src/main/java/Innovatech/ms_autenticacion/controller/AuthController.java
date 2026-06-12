package Innovatech.ms_autenticacion.controller;

import Innovatech.ms_autenticacion.model.AuthRequest;
import Innovatech.ms_autenticacion.model.AuthResponse;
import Innovatech.ms_autenticacion.model.HabilidadIdsRequest;
import Innovatech.ms_autenticacion.model.HabilidadInfo;
import Innovatech.ms_autenticacion.model.Usuario;
import Innovatech.ms_autenticacion.security.JwtUtil;
import Innovatech.ms_autenticacion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario) {
        try {
            Usuario newUser = authService.register(usuario);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<Usuario>> getAllUsers(@RequestParam(required = false) String rol) {
        return ResponseEntity.ok(authService.getAllUsers(rol));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Usuario> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(authService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{id}/habilidades")
    public ResponseEntity<List<HabilidadInfo>> getHabilidadesUsuario(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(authService.getHabilidadesByUsuario(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}/habilidades")
    public ResponseEntity<List<HabilidadInfo>> updateHabilidadesUsuario(@PathVariable Long id, @RequestBody HabilidadIdsRequest request) {
        try {
            return ResponseEntity.ok(authService.updateHabilidadesForUsuario(id, request.getHabilidadIds()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Usuario> updateUser(@PathVariable Long id, @RequestBody Usuario usuario) {
        try {
            Usuario updatedUser = authService.updateUser(id, usuario);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<Usuario> getMe(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String rut = jwtUtil.extractRut(token);
            return ResponseEntity.ok(authService.getMe(rut));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Falta el header Authorization con formato Bearer");
        }
        try {
            jwtUtil.extractRut(authHeader.substring(7)); // lanza excepcion si es invalido o expiro
            return ResponseEntity.ok("Token es válido");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token inválido o expirado");
        }
    }
}
