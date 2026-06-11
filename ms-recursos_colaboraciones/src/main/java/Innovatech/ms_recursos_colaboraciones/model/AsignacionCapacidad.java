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
@Table(name = "asignaciones_capacidad")
public class AsignacionCapacidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;

    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId; // Referencia al ID del proyecto en ms-gestion_proyectos

    @Column(name = "horas_asignadas", nullable = false)
    private Integer horasAsignadas; // Horas semanales asignadas a este proyecto

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    
    @Column(nullable = false)
    private String rolEnProyecto; // Ej: Frontend Developer, QA, etc.
}
