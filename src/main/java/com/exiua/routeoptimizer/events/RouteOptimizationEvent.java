package com.exiua.routeoptimizer.events;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Evento de optimización de rutas para integración con RabbitMQ
 */
public class RouteOptimizationEvent {
    
    public enum EventType {
        OPTIMIZATION_REQUESTED,
        OPTIMIZATION_STARTED,
        OPTIMIZATION_PROGRESS,
        OPTIMIZATION_COMPLETED,
        OPTIMIZATION_FAILED,
        OPTIMIZATION_CANCELLED
    }
    
    private String eventId;
    private String jobId;
    private String userId;
    private String routeId;
    private EventType eventType;
    private String status;
    private Integer progress;
    private String message;
    private List<POIData> pois;
    private RoutePreferencesData preferences;
    private OptimizationResultData result;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedCompletion;
    
    // Constructors
    public RouteOptimizationEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public RouteOptimizationEvent(String jobId, String userId, EventType eventType) {
        this();
        this.jobId = jobId;
        this.userId = userId;
        this.eventType = eventType;
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    // Nested classes for structured data
    public static class POIData {
        private Long id;
        private String name;
        private Double latitude;
        private Double longitude;
        private String category;
        private String subcategory;
        private Integer visitDuration;
        private Double cost;
        private Double rating;
        
        // Getters y setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getSubcategory() { return subcategory; }
        public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
        
        public Integer getVisitDuration() { return visitDuration; }
        public void setVisitDuration(Integer visitDuration) { this.visitDuration = visitDuration; }
        
        public Double getCost() { return cost; }
        public void setCost(Double cost) { this.cost = cost; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
    }
    
    public static class RoutePreferencesData {
        private String optimizeFor;
        private Integer maxTotalTime;
        private Double maxTotalCost;
        private Boolean accessibilityRequired;
        
        // Getters y setters
        public String getOptimizeFor() { return optimizeFor; }
        public void setOptimizeFor(String optimizeFor) { this.optimizeFor = optimizeFor; }
        
        public Integer getMaxTotalTime() { return maxTotalTime; }
        public void setMaxTotalTime(Integer maxTotalTime) { this.maxTotalTime = maxTotalTime; }
        
        public Double getMaxTotalCost() { return maxTotalCost; }
        public void setMaxTotalCost(Double maxTotalCost) { this.maxTotalCost = maxTotalCost; }
        
        public Boolean getAccessibilityRequired() { return accessibilityRequired; }
        public void setAccessibilityRequired(Boolean accessibilityRequired) { this.accessibilityRequired = accessibilityRequired; }
    }
    
    public static class OptimizationResultData {
        private String optimizedRouteId;
        private List<String> optimizedSequence;
        private Double totalDistanceKm;
        private Integer totalTimeMinutes;
        private Double totalCost;
        private Double optimizationScore;
        private String routeDescription;
        
        // Getters y setters
        public String getOptimizedRouteId() { return optimizedRouteId; }
        public void setOptimizedRouteId(String optimizedRouteId) { this.optimizedRouteId = optimizedRouteId; }
        
        public List<String> getOptimizedSequence() { return optimizedSequence; }
        public void setOptimizedSequence(List<String> optimizedSequence) { this.optimizedSequence = optimizedSequence; }
        
        public Double getTotalDistanceKm() { return totalDistanceKm; }
        public void setTotalDistanceKm(Double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
        
        public Integer getTotalTimeMinutes() { return totalTimeMinutes; }
        public void setTotalTimeMinutes(Integer totalTimeMinutes) { this.totalTimeMinutes = totalTimeMinutes; }
        
        public Double getTotalCost() { return totalCost; }
        public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
        
        public Double getOptimizationScore() { return optimizationScore; }
        public void setOptimizationScore(Double optimizationScore) { this.optimizationScore = optimizationScore; }
        
        public String getRouteDescription() { return routeDescription; }
        public void setRouteDescription(String routeDescription) { this.routeDescription = routeDescription; }
    }
    
    // Main getters y setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public List<POIData> getPois() { return pois; }
    public void setPois(List<POIData> pois) { this.pois = pois; }
    
    public RoutePreferencesData getPreferences() { return preferences; }
    public void setPreferences(RoutePreferencesData preferences) { this.preferences = preferences; }
    
    public OptimizationResultData getResult() { return result; }
    public void setResult(OptimizationResultData result) { this.result = result; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getEstimatedCompletion() { return estimatedCompletion; }
    public void setEstimatedCompletion(LocalDateTime estimatedCompletion) { this.estimatedCompletion = estimatedCompletion; }
}