package com.exiua.routeoptimizer.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO para construir RouteProcessingRequest enriquecido con datos de proveedores
 * Este DTO incluye toda la información necesaria para optimizar rutas
 */
@Data
public class EnrichedRouteProcessingRequestDTO {

    @JsonProperty("route_id")
    private String routeId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("pois")
    private List<EnrichedPOI> pois;
    
    @JsonProperty("preferences")
    private RoutePreferences preferences;
    
    @JsonProperty("constraints")
    private RouteConstraints constraints;

    public EnrichedRouteProcessingRequestDTO() {
        this.pois = new ArrayList<>();
        this.preferences = new RoutePreferences();
        this.constraints = new RouteConstraints();
    }

    /**
     * POI enriquecido con datos del proveedor
     */
    @Data
    public static class EnrichedPOI {
        @JsonProperty("poi_id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("category")
        private String category; // Categoría principal
        
        @JsonProperty("subcategory")
        private String subcategory;
        
        @JsonProperty("visit_duration")
        private Integer visitDuration; // en minutos
        
        @JsonProperty("cost")
        private Double cost; // Costo promedio del proveedor
        
        @JsonProperty("rating")
        private Double rating;
        
        @JsonProperty("opening_hours")
        private String openingHours;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("image_url")
        private String imageUrl;
        
        @JsonProperty("accessibility")
        private Boolean accessibility;
        
        @JsonProperty("provider_id")
        private Long providerId;
        
        @JsonProperty("provider_name")
        private String providerName;
        
        @JsonProperty("categories")
        private List<String> categories; // Todas las preferencias del proveedor
    }

    /**
     * Preferencias para optimización de ruta
     */
    @Data
    public static class RoutePreferences {
        @JsonProperty("optimize_for")
        private String optimizeFor = "distance"; // distance, time, cost, experience
        
        @JsonProperty("max_total_time")
        private Integer maxTotalTime; // en minutos
        
        @JsonProperty("max_total_cost")
        private Double maxTotalCost;
        
        @JsonProperty("preferred_categories")
        private List<String> preferredCategories; // Preferencias del turista
        
        @JsonProperty("avoid_categories")
        private List<String> avoidCategories;
        
        @JsonProperty("accessibility_required")
        private Boolean accessibilityRequired = false;

        public RoutePreferences() {
            this.preferredCategories = new ArrayList<>();
            this.avoidCategories = new ArrayList<>();
        }
    }

    /**
     * Restricciones para la ruta
     */
    @Data
    public static class RouteConstraints {
        @JsonProperty("start_location")
        private Location startLocation;
        
        @JsonProperty("end_location")
        private Location endLocation;
        
        @JsonProperty("start_time")
        private String startTime; // ISO 8601 format
        
        @JsonProperty("lunch_break_required")
        private Boolean lunchBreakRequired = false;
        
        @JsonProperty("lunch_break_duration")
        private Integer lunchBreakDuration = 60; // minutos
    }

    /**
     * Ubicación geográfica
     */
    @Data
    public static class Location {
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;

        public Location() {}

        public Location(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Método helper para agregar POIs desde lista de proveedores
     */
    public void addPOIsFromProviders(List<EnrichedPOI> providerPOIs) {
        if (this.pois == null) {
            this.pois = new ArrayList<>();
        }
        this.pois.addAll(providerPOIs);
    }

    /**
     * Método helper para establecer preferencias del turista
     */
    public void setTouristPreferences(List<String> touristPreferences) {
        if (this.preferences == null) {
            this.preferences = new RoutePreferences();
        }
        this.preferences.setPreferredCategories(touristPreferences);
    }

    /**
     * Método helper para calcular costo total estimado
     */
    public Double calculateEstimatedTotalCost() {
        if (this.pois == null || this.pois.isEmpty()) {
            return 0.0;
        }
        
        return this.pois.stream()
            .map(EnrichedPOI::getCost)
            .filter(cost -> cost != null && cost < 999999.0) // Excluir HIGH_COST_VALUE
            .mapToDouble(Double::doubleValue)
            .sum();
    }

    /**
     * Método helper para obtener todas las categorías únicas de los POIs
     */
    public List<String> getAllUniqueCategories() {
        if (this.pois == null || this.pois.isEmpty()) {
            return new ArrayList<>();
        }
        
        return this.pois.stream()
            .flatMap(poi -> poi.getCategories() != null ? poi.getCategories().stream() : new ArrayList<String>().stream())
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Método helper para filtrar POIs por categoría
     */
    public List<EnrichedPOI> filterPOIsByCategory(String category) {
        if (this.pois == null || this.pois.isEmpty()) {
            return new ArrayList<>();
        }
        
        return this.pois.stream()
            .filter(poi -> poi.getCategories() != null && poi.getCategories().contains(category))
            .collect(Collectors.toList());
    }

    /**
     * Método helper para filtrar POIs con costo válido (que tienen servicios)
     */
    public List<EnrichedPOI> filterPOIsWithValidCost() {
        if (this.pois == null || this.pois.isEmpty()) {
            return new ArrayList<>();
        }
        
        return this.pois.stream()
            .filter(poi -> poi.getCost() != null && poi.getCost() < 999999.0)
            .collect(Collectors.toList());
    }

    /**
     * Método helper para ordenar POIs por costo (menor a mayor)
     */
    public void sortPOIsByCost() {
        if (this.pois != null && !this.pois.isEmpty()) {
            this.pois.sort((p1, p2) -> {
                Double cost1 = p1.getCost() != null ? p1.getCost() : Double.MAX_VALUE;
                Double cost2 = p2.getCost() != null ? p2.getCost() : Double.MAX_VALUE;
                return cost1.compareTo(cost2);
            });
        }
    }
}
