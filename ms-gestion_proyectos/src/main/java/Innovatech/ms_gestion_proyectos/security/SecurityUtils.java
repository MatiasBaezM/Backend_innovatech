package Innovatech.ms_gestion_proyectos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String extractRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof AuthenticatedUser user) {
            return user.getRol();
        }
        return null;
    }

    public static Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof AuthenticatedUser user) {
            return user.getUserId();
        }
        return null;
    }
}
