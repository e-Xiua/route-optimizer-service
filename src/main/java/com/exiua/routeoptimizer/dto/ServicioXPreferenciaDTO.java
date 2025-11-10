package com.exiua.routeoptimizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ServicioXPreferenciaDTO {

    @JsonProperty("idServicioXPreferencia")
    private Long idServicioXPreferencia;
    
    @JsonProperty("idServicio")
    private Long idServicio;
    
    @JsonProperty("idPreferencia")
    private Long idPreferencia;
    
    // Nested PreferenciaDTO object
    @JsonProperty("nombrePreferencia")
    private PreferenciasDTO nombrePreferencia;
}
