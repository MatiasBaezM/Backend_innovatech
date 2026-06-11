package Innovatech.ms_gestion_proyectos.controller;

import Innovatech.ms_gestion_proyectos.model.EquipoRequest;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.security.JwtUtil;
import Innovatech.ms_gestion_proyectos.service.ProyectoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<Proyecto> getAllProyectos(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String rol = jwtUtil.extractRolFromHeader(authHeader);
        Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
        return proyectoService.getAllProyectos(rol, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> getProyectoById(@PathVariable Long id) {
        return proyectoService.getProyectoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Proyecto createProyecto(@RequestBody Proyecto proyecto) {
        return proyectoService.createProyecto(proyecto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> updateProyecto(@PathVariable Long id, @RequestBody Proyecto proyecto) {
        try {
            return ResponseEntity.ok(proyectoService.updateProyecto(id, proyecto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/equipo")
    public ResponseEntity<Proyecto> updateEquipo(@PathVariable Long id, @RequestBody EquipoRequest request,
                                                  @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!"ADMINISTRADOR".equals(jwtUtil.extractRolFromHeader(authHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return ResponseEntity.ok(proyectoService.updateEquipo(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProyecto(@PathVariable Long id) {
        proyectoService.deleteProyecto(id);
        return ResponseEntity.noContent().build();
    }
}
