package Innovatech.ms_analiticas.client;

import Innovatech.ms_analiticas.dto.AsignacionView;
import Innovatech.ms_analiticas.dto.ProyectoView;
import Innovatech.ms_analiticas.dto.TareaView;
import Innovatech.ms_analiticas.dto.TrabajadorView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Cliente HTTP que obtiene los datos de negocio desde los otros microservicios,
 * en lugar de leerlos por SQL directo (que rompia con una RDS por servicio).
 * Propaga el JWT entrante para que cada servicio autorice la peticion.
 *
 * Las URLs salen del ConfigMap (GESTION_PROYECTOS_SERVICE_URL,
 * RECURSOS_COLABORACIONES_SERVICE_URL); en local caen a los puertos por defecto.
 */
@Component
public class BackendClient {

    private final RestClient gestion;
    private final RestClient recursos;

    public BackendClient(
            @Value("${GESTION_PROYECTOS_SERVICE_URL:http://localhost:8083}") String gestionUrl,
            @Value("${RECURSOS_COLABORACIONES_SERVICE_URL:http://localhost:8082}") String recursosUrl) {
        this.gestion = RestClient.create(gestionUrl);
        this.recursos = RestClient.create(recursosUrl);
    }

    public List<ProyectoView> getProyectos(String auth) {
        return gestion.get().uri("/api/proyectos")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProyectoView>>() {});
    }

    public List<TareaView> getTareas(String auth) {
        return gestion.get().uri("/api/proyectos/tareas")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TareaView>>() {});
    }

    public List<TrabajadorView> getTrabajadores(String auth) {
        return recursos.get().uri("/api/trabajadores")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TrabajadorView>>() {});
    }

    public List<AsignacionView> getAsignaciones(String auth) {
        return recursos.get().uri("/api/asignaciones")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AsignacionView>>() {});
    }
}
