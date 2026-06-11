package Innovatech.ms_gestion_proyectos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proyectoId;

    private String titulo;
    private String descripcion;

    private LocalDate fechaCreacion;
    private LocalDate fechaLimite;

    private Long asignadoId;
    private String asignadoNombre;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @Column(length = 500)
    private String mensajeCorreccion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDate.now();
        if (estado == null) estado = Estado.POR_HACER;
    }

    public enum Prioridad { ALTA, MEDIA, BAJA }
    public enum Estado { POR_HACER, EN_PROGRESO, COMPLETADO, REVISADO }
}
