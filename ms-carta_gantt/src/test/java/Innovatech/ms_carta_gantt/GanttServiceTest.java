package Innovatech.ms_carta_gantt;

import Innovatech.ms_carta_gantt.client.BackendClient;
import Innovatech.ms_carta_gantt.dto.GanttProyectoDTO;
import Innovatech.ms_carta_gantt.dto.GanttTareaDTO;
import Innovatech.ms_carta_gantt.dto.ProyectoExternoDTO;
import Innovatech.ms_carta_gantt.dto.TareaExternaDTO;
import Innovatech.ms_carta_gantt.service.GanttService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GanttServiceTest {

    @Mock
    private BackendClient backendClient;

    @InjectMocks
    private GanttService ganttService;

    @Test
    void getGanttProyecto_cuandoBackendFalla_devuelveListaVacia() {
        when(backendClient.getProyecto(any(), anyString())).thenThrow(new RestClientException("down"));
        when(backendClient.getTareas(any(), anyString())).thenThrow(new RestClientException("down"));

        GanttProyectoDTO resultado = ganttService.getGanttProyecto(1L, "Bearer token-invalido");

        assertThat(resultado).isNotNull();
        assertThat(resultado.tareas()).isEmpty();
        assertThat(resultado.nombreProyecto()).isEqualTo("Proyecto 1");
    }

    @Test
    void getGanttProyecto_filtraTareasSinFechas() {
        ProyectoExternoDTO proyecto = new ProyectoExternoDTO();
        proyecto.setId(1L);
        proyecto.setNombre("Proyecto Test");
        proyecto.setEstado("EN_PROGRESO");

        TareaExternaDTO conFechas = new TareaExternaDTO();
        conFechas.setId(1L);
        conFechas.setTitulo("Con fechas");
        conFechas.setEstado("EN_PROGRESO");
        conFechas.setPrioridad("ALTA");
        conFechas.setFechaCreacion("2026-07-01");
        conFechas.setFechaLimite("2026-07-10");

        TareaExternaDTO sinFechas = new TareaExternaDTO();
        sinFechas.setId(2L);
        sinFechas.setTitulo("Sin fechas");
        sinFechas.setEstado("POR_HACER");
        sinFechas.setPrioridad("MEDIA");

        when(backendClient.getProyecto(any(), anyString())).thenReturn(proyecto);
        when(backendClient.getTareas(any(), anyString())).thenReturn(List.of(conFechas, sinFechas));

        GanttProyectoDTO resultado = ganttService.getGanttProyecto(1L, "Bearer token");

        assertThat(resultado.tareas()).hasSize(1);
        assertThat(resultado.tareas().get(0).titulo()).isEqualTo("Con fechas");
    }

    @Test
    void tareasConFechas_tienenOrdenCorrelativo() {
        GanttTareaDTO t1 = new GanttTareaDTO(1L, "T1", "EN_PROGRESO", "ALTA",
                "Juan", "2026-07-01", "2026-07-10", 8, 1);
        GanttTareaDTO t2 = new GanttTareaDTO(2L, "T2", "POR_HACER", "MEDIA",
                "Ana", "2026-07-05", "2026-07-15", 4, 2);

        assertThat(t1.orden()).isLessThan(t2.orden());
    }
}
