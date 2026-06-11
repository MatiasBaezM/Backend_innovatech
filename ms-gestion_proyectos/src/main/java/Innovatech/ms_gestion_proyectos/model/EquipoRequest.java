package Innovatech.ms_gestion_proyectos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoRequest {
    private Long gestorId;
    private String gestorNombre;
    private List<Colaborador> colaboradores;
}
