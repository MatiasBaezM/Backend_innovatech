package Innovatech.ms_autenticacion.config;

import Innovatech.ms_autenticacion.model.Usuario;
import Innovatech.ms_autenticacion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Optional<Usuario> adminOpt = usuarioRepository.findByRut("11.111.111-1");
        if (adminOpt.isEmpty()) {
            Usuario admin = Usuario.builder()
                    .rut("11.111.111-1")
                    .nombre("Administrador")
                    .clave(passwordEncoder.encode("admin123"))
                    .rol("ADMINISTRADOR")
                    .build();
            usuarioRepository.save(admin);
            System.out.println(
                    "====== Usuario administrador ('11.111.111-1'/'admin123') creado exitosamente en la base de datos. ======");
        }
    }
}
