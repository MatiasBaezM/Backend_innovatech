package Innovatech.ms_gestion_proyectos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {

    // Debe coincidir con el SECRET de ms-autenticacion para poder validar la firma del token
    private static final String SECRET = "EstaEsUnaClaveSecretaMuyLargaParaPoderFirmarElTokenJwtInnovatech2026";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String resolveToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    public String extractRol(String token) {
        return (String) extractAllClaims(token).get("rol");
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
