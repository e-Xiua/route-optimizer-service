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

        public String[] getCategories() {
            return categories;
        }

        public void setCategories(String[] categories) {
            this.categories = categories;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOpeningHours() {
            return openingHours;
        }

        public void setOpeningHours(String openingHours) {
            this.openingHours = openingHours;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Boolean getAccessibility() {
            return accessibility;
        }

        public void setAccessibility(Boolean accessibility) {
            this.accessibility = accessibility;
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

        public List<String> getPreferredCategories() {
            return preferredCategories;
        }

        public void setPreferredCategories(List<String> preferredCategories) {
            this.preferredCategories = preferredCategories;
        }

        public List<String> getAvoidCategories() {
            return avoidCategories;
        }

        public void setAvoidCategories(List<String> avoidCategories) {
            this.avoidCategories = avoidCategories;
        }

        public Integer getGroupSize() {
            return groupSize;
        }

        public void setGroupSize(Integer groupSize) {
            this.groupSize = groupSize;
        }

        public String getTouristType() {
            return touristType;
        }

        public void setTouristType(String touristType) {
            this.touristType = touristType;
        }

        public Double getAdventureLevel() {
            return adventureLevel;
        }

        public void setAdventureLevel(Double adventureLevel) {
            this.adventureLevel = adventureLevel;
        }

        public Double getCostSensitivity() {
            return costSensitivity;
        }

        public void setCostSensitivity(Double costSensitivity) {
            this.costSensitivity = costSensitivity;
        }

        public Double getSustainabilityMin() {
            return sustainabilityMin;
        }

        public void setSustainabilityMin(Double sustainabilityMin) {
            this.sustainabilityMin = sustainabilityMin;
        }

        public Double getMaxDistanceKm() {
            return maxDistanceKm;
        }

        public void setMaxDistanceKm(Double maxDistanceKm) {
            this.maxDistanceKm = maxDistanceKm;
        }
    }

    /**
     * Route constraints DTO
     */
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

        public LocationDTO getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(LocationDTO startLocation) {
            this.startLocation = startLocation;
        }

        public LocationDTO getEndLocation() {
            return endLocation;
        }

        public void setEndLocation(LocationDTO endLocation) {
            this.endLocation = endLocation;
        }
    }

    /**
     * Location coordinates DTO
     */
    public static class LocationDTO {
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;

        public LocationDTO() {}

        public LocationDTO(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
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
    }
}