package Innovatech.ms_recursos_colaboraciones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evaluaciones")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Trabajador evaluador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluado_id", nullable = false)
    private Trabajador evaluado;

    @Column(nullable = false)
    private LocalDate fecha;

    private Integer puntaje; // Ej: de 1 a 5

    @Column(length = 500)
    private String comentarios;
}
