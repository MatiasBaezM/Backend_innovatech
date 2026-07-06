package Innovatech.ms_carta_gantt.client;

import Innovatech.ms_carta_gantt.dto.ProyectoExternoDTO;
import Innovatech.ms_carta_gantt.dto.TareaExternaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Cliente HTTP que obtiene proyecto y tareas desde ms-gestion_proyectos para
 * armar la carta Gantt. Propaga el JWT entrante para que el servicio destino
 * autorice la peticion (mismo patron que BackendClient de ms-analiticas).
 *
 * La URL sale del ConfigMap (GESTION_PROYECTOS_SERVICE_URL); en local cae al
 * puerto por defecto.
 */
@Component
public class BackendClient {

    private final RestClient gestion;

    public BackendClient(
            @Value("${GESTION_PROYECTOS_SERVICE_URL:http://localhost:8083}") String gestionUrl) {
        this.gestion = RestClient.create(gestionUrl);
    }

    public ProyectoExternoDTO getProyecto(Long proyectoId, String auth) {
        return gestion.get().uri("/api/proyectos/{id}", proyectoId)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(ProyectoExternoDTO.class);
    }

    public List<TareaExternaDTO> getTareas(Long proyectoId, String auth) {
        return gestion.get().uri("/api/proyectos/{id}/tareas", proyectoId)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TareaExternaDTO>>() {});
    }
}
