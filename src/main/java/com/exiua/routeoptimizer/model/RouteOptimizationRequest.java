package com.exiua.routeoptimizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Route optimization request model - Updated to align with frontend and processing service
 */
public class RouteOptimizationRequest {
    
    @JsonProperty("routeId")
    private String routeId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("pois")
    @NotNull(message = "POIs list is required")
    @NotEmpty(message = "POIs list cannot be empty")
    @Valid
    public List<POI> pois;
    
    @JsonProperty("preferences")
    private RoutePreferences preferences;
    
    @JsonProperty("constraints")
    private RouteConstraints constraints;

    // Constructors
    public RouteOptimizationRequest() {}
    
    public RouteOptimizationRequest(List<POI> pois) {
        this.pois = pois;
    }

    // Inner classes
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
        
        // Getters and Setters
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
    
    public static class RoutePreferences {
        @JsonProperty("optimizeFor")
        private String optimizeFor = "distance"; // "time", "distance", "cost", "experience"
        
        @JsonProperty("maxTotalTime")
        private Integer maxTotalTime; // in minutes
        
        @JsonProperty("maxTotalCost")
        private Double maxTotalCost;
        
        @JsonProperty("preferredCategories")
        private List<String> preferredCategories;
        
        @JsonProperty("avoidCategories")
        private List<String> avoidCategories;
        
        @JsonProperty("accessibilityRequired")
        private Boolean accessibilityRequired = false;
        
        // Legacy fields for backward compatibility
        @JsonProperty("avoid_tolls")
        private Boolean avoidTolls;
        
        @JsonProperty("transport_mode")
        private String transportMode = "driving";
        
        // Getters and Setters
        public String getOptimizeFor() { return optimizeFor; }
        public void setOptimizeFor(String optimizeFor) { this.optimizeFor = optimizeFor; }
        
        public Integer getMaxTotalTime() { return maxTotalTime; }
        public void setMaxTotalTime(Integer maxTotalTime) { this.maxTotalTime = maxTotalTime; }
        
        public Double getMaxTotalCost() { return maxTotalCost; }
        public void setMaxTotalCost(Double maxTotalCost) { this.maxTotalCost = maxTotalCost; }
        
        public List<String> getPreferredCategories() { return preferredCategories; }
        public void setPreferredCategories(List<String> preferredCategories) { this.preferredCategories = preferredCategories; }
        
        public List<String> getAvoidCategories() { return avoidCategories; }
        public void setAvoidCategories(List<String> avoidCategories) { this.avoidCategories = avoidCategories; }
        
        public Boolean getAccessibilityRequired() { return accessibilityRequired; }
        public void setAccessibilityRequired(Boolean accessibilityRequired) { this.accessibilityRequired = accessibilityRequired; }
        
        public Boolean getAvoidTolls() { return avoidTolls; }
        public void setAvoidTolls(Boolean avoidTolls) { this.avoidTolls = avoidTolls; }
        
        public String getTransportMode() { return transportMode; }
        public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
        public Integer getGroupSize() {
            return 15;
        }
        public String getTouristType() {
            return "cultural";
        }
        public Double getAdventureLevel() {
            return 3.0;
        }
        public Double getCostSensitivity() {
            return 2.0;
        }
        public Double getSustainabilityMin() {
            return 1.0;
        }
        public Double getMaxDistanceKm() {
            return 10000000000000.0;
        }
    }
    
    public static class RouteConstraints {
        @JsonProperty("startLocation")
        private Location startLocation;
        
        @JsonProperty("endLocation")
        private Location endLocation;
        
        @JsonProperty("startTime")
        private String startTime;
        
        @JsonProperty("lunchBreakRequired")
        private Boolean lunchBreakRequired = false;
        
        @JsonProperty("lunchBreakDuration")
        private Integer lunchBreakDuration = 60;
        
        // Getters and Setters
        public Location getStartLocation() { return startLocation; }
        public void setStartLocation(Location startLocation) { this.startLocation = startLocation; }
        
        public Location getEndLocation() { return endLocation; }
        public void setEndLocation(Location endLocation) { this.endLocation = endLocation; }
        
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        
        public Boolean getLunchBreakRequired() { return lunchBreakRequired; }
        public void setLunchBreakRequired(Boolean lunchBreakRequired) { this.lunchBreakRequired = lunchBreakRequired; }
        
        public Integer getLunchBreakDuration() { return lunchBreakDuration; }
        public void setLunchBreakDuration(Integer lunchBreakDuration) { this.lunchBreakDuration = lunchBreakDuration; }
    }

    // Getters and Setters
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public List<POI> getPois() { return pois; }
    public void setPois(List<POI> pois) { this.pois = pois; }
    
    public RoutePreferences getPreferences() { return preferences; }
    public void setPreferences(RoutePreferences preferences) { this.preferences = preferences; }
    
    public RouteConstraints getConstraints() { return constraints; }
    public void setConstraints(RouteConstraints constraints) { this.constraints = constraints; }
}