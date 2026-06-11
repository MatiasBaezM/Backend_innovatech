package Innovatech.ms_recursos_colaboraciones.config;

import Innovatech.ms_recursos_colaboraciones.model.Habilidad;
import Innovatech.ms_recursos_colaboraciones.model.Trabajador;
import Innovatech.ms_recursos_colaboraciones.repository.HabilidadRepository;
import Innovatech.ms_recursos_colaboraciones.repository.TrabajadorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(TrabajadorRepository trabajadorRepository, HabilidadRepository habilidadRepository) {
        return args -> {
            if (trabajadorRepository.count() == 0) {
                Trabajador t1 = Trabajador.builder()
                        .rut("11.111.111-1")
                        .nombre("Juan Pérez")
                        .email("juan@innovatech.cl")
                        .cargo("Desarrollador Backend")
                        .departamento("Ingeniería")
                        .tarifaHora(25.0)
                        .build();

                Trabajador t2 = Trabajador.builder()
                        .rut("22.222.222-2")
                        .nombre("María Silva")
                        .email("maria@innovatech.cl")
                        .cargo("Diseñadora UX/UI")
                        .departamento("Diseño")
                        .tarifaHora(22.0)
                        .build();

                trabajadorRepository.saveAll(Arrays.asList(t1, t2));
            }

            if (habilidadRepository.count() == 0) {
                habilidadRepository.saveAll(Arrays.asList(
                        Habilidad.builder().nombre("Desarrollador Backend").color("#6366f1").build(),
                        Habilidad.builder().nombre("Desarrollador Frontend").color("#0ea5e9").build(),
                        Habilidad.builder().nombre("DBA").color("#10b981").build(),
                        Habilidad.builder().nombre("Analista QA").color("#f59e0b").build(),
                        Habilidad.builder().nombre("Diseñador UX/UI").color("#ec4899").build(),
                        Habilidad.builder().nombre("DevOps").color("#ef4444").build(),
                        Habilidad.builder().nombre("Scrum Master").color("#8b5cf6").build(),
                        Habilidad.builder().nombre("Analista de Negocio").color("#14b8a6").build()
                ));
            }
        };
    }
}
