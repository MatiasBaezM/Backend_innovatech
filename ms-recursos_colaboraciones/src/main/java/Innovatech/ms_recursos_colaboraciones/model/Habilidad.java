package Innovatech.ms_recursos_colaboraciones.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "habilidades")
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // e.g., "Java", "React", "Scrum Master"

    private String descripcion;

    @Column(nullable = false)
    private String color; // hex, ej: #6366f1

    @JsonIgnore
    @ManyToMany(mappedBy = "habilidades")
    private Set<Trabajador> trabajadores = new HashSet<>();
}
