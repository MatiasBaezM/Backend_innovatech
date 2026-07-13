package Innovatech.ms_gestion_proyectos.service;

import Innovatech.ms_gestion_proyectos.model.EquipoRequest;
import Innovatech.ms_gestion_proyectos.model.Proyecto;
import Innovatech.ms_gestion_proyectos.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    public List<Proyecto> getAllProyectos(String rol, Long userId) {
        List<Proyecto> proyectos = proyectoRepository.findAll();

        if ("GESTOR_PROYECTOS".equals(rol) && userId != null) {
            return proyectos.stream()
                    .filter(p -> userId.equals(p.getGestorId()))
                    .toList();
        }

        if ("COLABORADOR".equals(rol) && userId != null) {
            return proyectos.stream()
                    .filter(p -> p.getColaboradores() != null
                            && p.getColaboradores().stream().anyMatch(c -> userId.equals(c.getId())))
                    .toList();
        }

        return proyectos;
    }

    public Optional<Proyecto> getProyectoById(Long id) {
        return proyectoRepository.findById(id);
    }

    public Proyecto createProyecto(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Proyecto updateProyecto(Long id, Proyecto proyectoDetails) {
        return proyectoRepository.findById(id).map(proyecto -> {
            proyecto.setNombre(proyectoDetails.getNombre());
            proyecto.setDescripcion(proyectoDetails.getDescripcion());
            proyecto.setEstado(proyectoDetails.getEstado());
            proyecto.setFechaInicio(proyectoDetails.getFechaInicio());
            proyecto.setFechaFin(proyectoDetails.getFechaFin());
            proyecto.setRutResponsable(proyectoDetails.getRutResponsable());
            return proyectoRepository.save(proyecto);
        }).orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    public Proyecto updateEquipo(Long id, EquipoRequest request) {
        return proyectoRepository.findById(id).map(proyecto -> {
            proyecto.setGestorId(request.getGestorId());
            proyecto.setGestorNombre(request.getGestorNombre());
            proyecto.setColaboradores(request.getColaboradores() != null ? request.getColaboradores() : new ArrayList<>());
            return proyectoRepository.save(proyecto);
        }).orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    public void deleteProyecto(Long id) {
        proyectoRepository.deleteById(id);
    }
}
