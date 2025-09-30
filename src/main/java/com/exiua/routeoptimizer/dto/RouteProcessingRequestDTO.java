package com.exiua.routeoptimizer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for communicating with route-processing-service
 */
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

    // Constructors
    public RouteProcessingRequestDTO() {}

    // Getters and Setters
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<ProcessingPOIDTO> getPois() {
        return pois;
    }

    public void setPois(List<ProcessingPOIDTO> pois) {
        this.pois = pois;
    }

    public RoutePreferencesDTO getPreferences() {
        return preferences;
    }

    public void setPreferences(RoutePreferencesDTO preferences) {
        this.preferences = preferences;
    }

    public RouteConstraintsDTO getConstraints() {
        return constraints;
    }

    public void setConstraints(RouteConstraintsDTO constraints) {
        this.constraints = constraints;
    }

    /**
     * POI DTO for processing service communication
     */
    public static class ProcessingPOIDTO {
        
        @JsonProperty("poi_id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("category")
        private String category;
        
        @JsonProperty("subcategory")
        private String subcategory;
        
        @JsonProperty("visit_duration")
        private Integer visitDuration;
        
        @JsonProperty("cost")
        private Double cost;
        
        @JsonProperty("rating")
        private Double rating;
        
        @JsonProperty("provider_id")
        private Long providerId;
        
        @JsonProperty("provider_name")
        private String providerName;

        // Constructors
        public ProcessingPOIDTO() {}

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSubcategory() {
            return subcategory;
        }

        public void setSubcategory(String subcategory) {
            this.subcategory = subcategory;
        }

        public Integer getVisitDuration() {
            return visitDuration;
        }

        public void setVisitDuration(Integer visitDuration) {
            this.visitDuration = visitDuration;
        }

        public Double getCost() {
            return cost;
        }

        public void setCost(Double cost) {
            this.cost = cost;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public Long getProviderId() {
            return providerId;
        }

        public void setProviderId(Long providerId) {
            this.providerId = providerId;
        }

        public String getProviderName() {
            return providerName;
        }

        public void setProviderName(String providerName) {
            this.providerName = providerName;
        }
    }

    /**
     * Route preferences DTO
     */
    public static class RoutePreferencesDTO {
        @JsonProperty("optimize_for")
        private String optimizeFor = "distance";
        
        @JsonProperty("max_total_time")
        private Integer maxTotalTime;
        
        @JsonProperty("max_total_cost")
        private Double maxTotalCost;
        
        @JsonProperty("accessibility_required")
        private Boolean accessibilityRequired = false;

        // Getters and Setters
        public String getOptimizeFor() {
            return optimizeFor;
        }

        public void setOptimizeFor(String optimizeFor) {
            this.optimizeFor = optimizeFor;
        }

        public Integer getMaxTotalTime() {
            return maxTotalTime;
        }

        public void setMaxTotalTime(Integer maxTotalTime) {
            this.maxTotalTime = maxTotalTime;
        }

        public Double getMaxTotalCost() {
            return maxTotalCost;
        }

        public void setMaxTotalCost(Double maxTotalCost) {
            this.maxTotalCost = maxTotalCost;
        }

        public Boolean getAccessibilityRequired() {
            return accessibilityRequired;
        }

        public void setAccessibilityRequired(Boolean accessibilityRequired) {
            this.accessibilityRequired = accessibilityRequired;
        }
    }

    /**
     * Route constraints DTO
     */
    public static class RouteConstraintsDTO {
        @JsonProperty("start_time")
        private String startTime;
        
        @JsonProperty("lunch_break_required")
        private Boolean lunchBreakRequired = false;
        
        @JsonProperty("lunch_break_duration")
        private Integer lunchBreakDuration = 60;

        // Getters and Setters
        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public Boolean getLunchBreakRequired() {
            return lunchBreakRequired;
        }

        public void setLunchBreakRequired(Boolean lunchBreakRequired) {
            this.lunchBreakRequired = lunchBreakRequired;
        }

        public Integer getLunchBreakDuration() {
            return lunchBreakDuration;
        }

        public void setLunchBreakDuration(Integer lunchBreakDuration) {
            this.lunchBreakDuration = lunchBreakDuration;
        }
    }
}