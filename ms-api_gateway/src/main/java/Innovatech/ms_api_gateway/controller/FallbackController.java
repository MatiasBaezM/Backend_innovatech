package Innovatech.ms_api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador de fallback para el Circuit Breaker.
 * Cuando un microservicio no está disponible o tarda demasiado,
 * el Gateway redirige aquí en lugar de devolver un error genérico.
 */
@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public ResponseEntity<Map<String, String>> fallbackGet() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "error", "Servicio no disponible",
                        "message", "El servicio solicitado no está disponible en este momento. Intente nuevamente más tarde."
                ));
    }

    @PostMapping("/fallback")
    public ResponseEntity<Map<String, String>> fallbackPost() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", "503",
                        "error", "Servicio no disponible",
                        "message", "El servicio solicitado no está disponible en este momento. Intente nuevamente más tarde."
                ));
    }
}
