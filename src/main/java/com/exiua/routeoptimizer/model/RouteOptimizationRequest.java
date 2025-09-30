package com.exiua.routeoptimizer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Route optimization request model
 */
public class RouteOptimizationRequest {
    
    @JsonProperty("route_id")
    private String routeId;
    
    @JsonProperty("pois")
    @NotNull(message = "POIs list is required")
    @NotEmpty(message = "POIs list cannot be empty")
    @Valid
    private List<POI> pois;
    
    @JsonProperty("start_location")
    private Location startLocation;
    
    @JsonProperty("end_location") 
    private Location endLocation;
    
    @JsonProperty("max_travel_time")
    private Integer maxTravelTime; // in minutes
    
    @JsonProperty("preferences")
    private RoutePreferences preferences;
    
    @JsonProperty("user_id")
    private String userId;

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
        @JsonProperty("optimize_for")
        private String optimizeFor; // "time", "distance", "cost"
        
        @JsonProperty("avoid_tolls")
        private Boolean avoidTolls;
        
        @JsonProperty("transport_mode")
        private String transportMode; // "driving", "walking", "transit"
        
        // Getters and Setters
        public String getOptimizeFor() { return optimizeFor; }
        public void setOptimizeFor(String optimizeFor) { this.optimizeFor = optimizeFor; }
        
        public Boolean getAvoidTolls() { return avoidTolls; }
        public void setAvoidTolls(Boolean avoidTolls) { this.avoidTolls = avoidTolls; }
        
        public String getTransportMode() { return transportMode; }
        public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
        public Integer getMaxTotalTime() {
            return 10000;
        }
        public Double getMaxTotalCost() {
            return 100.0;
        }
        }
    

    // Getters and Setters
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    
    public List<POI> getPois() { return pois; }
    public void setPois(List<POI> pois) { this.pois = pois; }
    
    public Location getStartLocation() { return startLocation; }
    public void setStartLocation(Location startLocation) { this.startLocation = startLocation; }
    
    public Location getEndLocation() { return endLocation; }
    public void setEndLocation(Location endLocation) { this.endLocation = endLocation; }
    
    public Integer getMaxTravelTime() { return maxTravelTime; }
    public void setMaxTravelTime(Integer maxTravelTime) { this.maxTravelTime = maxTravelTime; }
    
    public RoutePreferences getPreferences() { return preferences; }
    public void setPreferences(RoutePreferences preferences) { this.preferences = preferences; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}