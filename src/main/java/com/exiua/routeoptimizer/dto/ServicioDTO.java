package com.exiua.routeoptimizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ServicioDTO {
    @JsonProperty("_idServicio")
    private Long idServicio;
    
    @JsonProperty("_idProveedor")
    private Long idProveedor;
    
    private String nombre;
    private String descripcion;
    private Double precio;
    private String imagen;
    private String horario;
    private boolean estado;
    
    @JsonProperty("tiempoAproximado")
    private Integer tiempoAproximado; // Tiempo aproximado de visita en minutos
}
