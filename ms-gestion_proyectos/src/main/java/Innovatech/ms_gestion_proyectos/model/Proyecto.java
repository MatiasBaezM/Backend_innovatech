package Innovatech.ms_gestion_proyectos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private String estado;
    private LocalDate fechaInicio;

    // Campo para asignar un usuario responsable (por su RUT)
    private String rutResponsable;

    private Long gestorId;
    private String gestorNombre;

    @ElementCollection
    @CollectionTable(name = "proyecto_colaboradores", joinColumns = @JoinColumn(name = "proyecto_id"))
    @Builder.Default
    private List<Colaborador> colaboradores = new ArrayList<>();
}
