package Innovatech.ms_autenticacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String clave;

    private String rol;

    @Column(unique = true)
    private String correo;

    @ElementCollection
    @CollectionTable(name = "usuario_habilidad", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "habilidad_id")
    @Builder.Default
    private Set<Long> habilidadIds = new HashSet<>();

    @Transient
    private List<HabilidadInfo> habilidades;
}
