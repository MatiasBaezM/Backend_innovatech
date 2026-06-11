package Innovatech.ms_gestion_proyectos.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Colaborador {
    private Long id;
    private String nombre;
}
