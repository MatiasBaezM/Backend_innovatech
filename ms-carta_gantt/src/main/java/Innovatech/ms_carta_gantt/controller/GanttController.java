package Innovatech.ms_carta_gantt.controller;

import Innovatech.ms_carta_gantt.dto.GanttProyectoDTO;
import Innovatech.ms_carta_gantt.service.GanttService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gantt")
@RequiredArgsConstructor
public class GanttController {

    private final GanttService ganttService;

    /**
     * GET /api/gantt/proyectos/{id}
     * Devuelve la carta Gantt del proyecto indicado. Requiere JWT valido en
     * la cabecera Authorization.
     */
    @GetMapping("/proyectos/{id}")
    public ResponseEntity<GanttProyectoDTO> getGantt(
            @PathVariable Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        return ResponseEntity.ok(ganttService.getGanttProyecto(id, auth));
    }
}
