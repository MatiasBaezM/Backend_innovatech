package Innovatech.ms_gestion_proyectos.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private final String rol;
    private final Long userId;
}
