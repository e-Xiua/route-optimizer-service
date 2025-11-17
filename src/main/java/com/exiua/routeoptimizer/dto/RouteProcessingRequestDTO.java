package com.exiua.routeoptimizer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for communicating with route-processing-service
 */
@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteProcessingRequestDTO {
    
    @JsonProperty("route_id")
    private String routeId;
    
    @JsonProperty("user_id") 
    private String userId;
    
    @JsonProperty("pois")
    private List<ProcessingPOIDTO> pois;
    
    @JsonProperty("preferences")
    private RoutePreferencesDTO preferences;
    
    @JsonProperty("constraints")
    private RouteConstraintsDTO constraints;

    
    /**
     * POI DTO for processing service communication
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor 
    @Setter
    public static class ProcessingPOIDTO {
        
        @JsonProperty("poi_id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("categories")
        private String[] categories;
        
        @JsonProperty("category")
        private String category; // Main category (single)
        
        @JsonProperty("subcategory")
        private String subcategory;
        
        @JsonProperty("visit_duration")
        private Integer visitDuration;
        
        @JsonProperty("cost")
        private Double cost;
        
        @JsonProperty("rating")
        private Double rating;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("opening_hours")
        private String openingHours;
        
        @JsonProperty("image_url")
        private String imageUrl;
        
        @JsonProperty("accessibility")
        private Boolean accessibility;
        
        @JsonProperty("provider_id")
        private Long providerId;
        
        @JsonProperty("provider_name")
        private String providerName;

    }

    /**
     * Route preferences DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class RoutePreferencesDTO {
        @JsonProperty("optimize_for")
        private String optimizeFor = "distance";
        
        @JsonProperty("max_total_time")
        private Integer maxTotalTime;
        
        @JsonProperty("max_total_cost")
        private Double maxTotalCost;
        
        @JsonProperty("preferred_categories")
        private List<String> preferredCategories;
        
        @JsonProperty("avoid_categories")
        private List<String> avoidCategories;
        
        @JsonProperty("group_size")
        private Integer groupSize = 1;
        
        @JsonProperty("tourist_type")
        private String touristType = "custom";
        
        @JsonProperty("accessibility_required")
        private Boolean accessibilityRequired = false;
        
        @JsonProperty("adventure_level")
        private Double adventureLevel = 50.0;
        
        @JsonProperty("cost_sensitivity")
        private Double costSensitivity = 50.0;
        
        @JsonProperty("sustainability_min")
        private Double sustainabilityMin = 60.0;
        
        @JsonProperty("max_distance_km")
        private Double maxDistanceKm = 80.0;

      
    }

    /**
     * Route constraints DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class RouteConstraintsDTO {
        @JsonProperty("start_location")
        private LocationDTO startLocation;
        
        @JsonProperty("end_location")
        private LocationDTO endLocation;
        
        @JsonProperty("start_time")
        private String startTime;
        
        @JsonProperty("lunch_break_required")
        private Boolean lunchBreakRequired = false;
        
        @JsonProperty("lunch_break_duration")
        private Integer lunchBreakDuration = 60;

    }

    /**
     * Location coordinates DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class LocationDTO {
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;

    }
}