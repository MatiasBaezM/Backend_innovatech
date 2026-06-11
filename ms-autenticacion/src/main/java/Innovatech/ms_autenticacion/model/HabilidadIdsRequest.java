package Innovatech.ms_autenticacion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabilidadIdsRequest {
    private Set<Long> habilidadIds;
}
