package Innovatech.ms_autenticacion.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // You should inject this from properties in a real app
    private static final String SECRET = "EstaEsUnaClaveSecretaMuyLargaParaPoderFirmarElTokenJwtInnovatech2026";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String rut) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, rut);
    }

    public String generateToken(Long id, String rut, String rol, String nombre) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("rol", rol != null ? rol : "COLABORADOR");
        claims.put("nombre", nombre != null ? nombre : rut);
        return createToken(claims, rut);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String rut) {
        final String extractedRut = extractRut(token);
        return (extractedRut.equals(rut) && !isTokenExpired(token));
    }

    public String extractRut(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRol(String token) {
        return (String) extractAllClaims(token).get("rol");
    }

    public Long extractUserId(String token) {
        Object id = extractAllClaims(token).get("id");
        return id != null ? ((Number) id).longValue() : null;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
