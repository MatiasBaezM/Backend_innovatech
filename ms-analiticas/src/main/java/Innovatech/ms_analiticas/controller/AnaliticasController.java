package Innovatech.ms_analiticas.controller;

import Innovatech.ms_analiticas.dto.CargaTrabajoDTO;
import Innovatech.ms_analiticas.dto.CostoProyectoDTO;
import Innovatech.ms_analiticas.dto.GrupoConteoDTO;
import Innovatech.ms_analiticas.dto.ResumenDTO;
import Innovatech.ms_analiticas.service.AnaliticasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analiticas")
@RequiredArgsConstructor
public class AnaliticasController {

    private final AnaliticasService analiticasService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenDTO> getResumen(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(analiticasService.getResumen(auth));
    }

    @GetMapping("/proyectos")
    public ResponseEntity<List<GrupoConteoDTO>> getProyectosPorEstado(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(analiticasService.getProyectosPorEstado(auth));
    }

    @GetMapping("/tareas/prioridad")
    public ResponseEntity<List<GrupoConteoDTO>> getTareasPorPrioridad(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(analiticasService.getTareasPorPrioridad(auth));
    }

    @GetMapping("/tareas/estado")
    public ResponseEntity<List<GrupoConteoDTO>> getTareasPorEstado(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(analiticasService.getTareasPorEstado(auth));
    }

    @GetMapping("/carga-trabajo")
    public ResponseEntity<List<CargaTrabajoDTO>> getCargaTrabajo(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(analiticasService.getCargaTrabajo(auth));
    }

    @GetMapping("/costos")
    public ResponseEntity<List<CostoProyectoDTO>> getCostosProyectos(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(analiticasService.getCostosProyectos(auth));
    }
}
