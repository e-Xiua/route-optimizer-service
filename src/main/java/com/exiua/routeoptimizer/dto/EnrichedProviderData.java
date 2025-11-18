package com.exiua.routeoptimizer.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
public class EnrichedProviderData {
    private ProveedorDTO provider;
    private List<ServicioDTO> services;
    private Double averageCost;
    private Integer averageVisitDuration;
    private List<String> categories;
    private Map<String, Object> metadata;

    public EnrichedProviderData() {
        this.services = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

}