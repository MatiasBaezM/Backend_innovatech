package Innovatech.ms_gestion_proyectos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private LocalDateTime fechaCreacion;

    // Trazabilidad de quien la genero y a que tarea/proyecto pertenece.
    // Todos nullable: las actividades de texto libre (sin tarea asociada) los dejan en null.
    private Long trabajadorId;
    private String trabajadorNombre;
    private Long proyectoId;
    private String proyectoNombre;
    private Long tareaId;

    // Tipo de evento: ASIGNADA, EN_PROGRESO, COMPLETADO, REVISADO, RECHAZADO, GENERAL
    private String tipo;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
