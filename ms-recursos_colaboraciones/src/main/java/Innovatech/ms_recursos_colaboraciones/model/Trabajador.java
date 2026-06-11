package Innovatech.ms_recursos_colaboraciones.model;

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
@Table(name = "trabajadores")
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut; // Referencia al microservicio de autenticación

    @Column(nullable = false)
    private String nombre;

    private String email;
    private String telefono;

    private String cargo;
    private String departamento;

    @Column(name = "tarifa_hora")
    private Double tarifaHora;

    @ManyToMany(mappedBy = "trabajadores")
    private Set<Equipo> equipos = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "trabajador_habilidad",
        joinColumns = @JoinColumn(name = "trabajador_id"),
        inverseJoinColumns = @JoinColumn(name = "habilidad_id")
    )
    private Set<Habilidad> habilidades = new HashSet<>();
}
