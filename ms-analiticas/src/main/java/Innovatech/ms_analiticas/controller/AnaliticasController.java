package Innovatech.ms_analiticas.controller;

import Innovatech.ms_analiticas.dto.CargaTrabajoDTO;
import Innovatech.ms_analiticas.dto.CostoProyectoDTO;
import Innovatech.ms_analiticas.dto.GrupoConteoDTO;
import Innovatech.ms_analiticas.dto.ResumenDTO;
import Innovatech.ms_analiticas.service.AnaliticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analiticas")
public class AnaliticasController {

    @Autowired
    private AnaliticasService analiticasService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenDTO> getResumen() {
        return ResponseEntity.ok(analiticasService.getResumen());
    }

    @GetMapping("/proyectos")
    public ResponseEntity<List<GrupoConteoDTO>> getProyectosPorEstado() {
        return ResponseEntity.ok(analiticasService.getProyectosPorEstado());
    }

    @GetMapping("/tareas/prioridad")
    public ResponseEntity<List<GrupoConteoDTO>> getTareasPorPrioridad() {
        return ResponseEntity.ok(analiticasService.getTareasPorPrioridad());
    }

    @GetMapping("/tareas/estado")
    public ResponseEntity<List<GrupoConteoDTO>> getTareasPorEstado() {
        return ResponseEntity.ok(analiticasService.getTareasPorEstado());
    }

    @GetMapping("/carga-trabajo")
    public ResponseEntity<List<CargaTrabajoDTO>> getCargaTrabajo() {
        return ResponseEntity.ok(analiticasService.getCargaTrabajo());
    }

    @GetMapping("/costos")
    public ResponseEntity<List<CostoProyectoDTO>> getCostosProyectos() {
        return ResponseEntity.ok(analiticasService.getCostosProyectos());
    }
}
