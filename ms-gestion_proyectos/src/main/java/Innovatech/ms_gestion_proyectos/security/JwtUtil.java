package Innovatech.ms_gestion_proyectos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    // Debe coincidir con el jwt.secret de ms-autenticacion para poder validar la firma del token.
    // Se inyecta desde la propiedad jwt.secret (variable de entorno JWT_SECRET en produccion).
    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String resolveToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    public String extractRol(String token) {
        return (String) extractAllClaims(token).get("rol");
    }

    public String extractRut(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object id = extractAllClaims(token).get("id");
        return id != null ? ((Number) id).longValue() : null;
    }

    public String extractRolFromHeader(String authHeader) {
        String token = resolveToken(authHeader);
        return token != null ? extractRol(token) : null;
    }

    public Long extractUserIdFromHeader(String authHeader) {
        String token = resolveToken(authHeader);
        return token != null ? extractUserId(token) : null;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
