package com.exiua.routeoptimizer.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * POI (Point of Interest) model that matches the Python service structure
 */
public class POI {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    @NotNull(message = "Name is required")
    private String name;
    
    @JsonProperty("latitude")
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @JsonProperty("longitude") 
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @JsonProperty("categories")
    private String[] categories;
    
    @JsonProperty("subcategory")
    private String subcategory;
    
    @JsonProperty("rating")
    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating;
    
    @JsonProperty("visitDuration")
    private Integer visitDuration; // in minutes - frontend uses camelCase
    
    @JsonProperty("cost")
    private Double cost;
    
    @JsonProperty("openingHours")
    private String openingHours; // frontend uses camelCase
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("accessibility")
    private Boolean accessibility = true;
    
    @JsonProperty("providerId")
    private Long providerId; // frontend uses camelCase
    
    @JsonProperty("providerName")
    private String providerName; // frontend uses camelCase
    
    // Legacy fields for backward compatibility
    @JsonProperty("visit_duration")
    private Integer visitDurationSnake; // snake_case for backend compatibility
    
    @JsonProperty("opening_hours")
    private String openingHoursSnake; // snake_case for backend compatibility
    
    @JsonProperty("provider_id")
    private Long providerIdSnake; // snake_case for backend compatibility
    
    @JsonProperty("provider_name")
    private String providerNameSnake; // snake_case for backend compatibility
    
    @JsonProperty("price_level")
    private Integer priceLevel; // 1-4 scale
    
    @JsonProperty("services")
    private String services;

    // Constructors
    public POI() {}
    
    public POI(Long id, String name, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String[] getCategories() { return categories; }
    public void setCategories(String[] categories) { this.categories = categories; }
    
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getVisitDuration() { return visitDuration; }
    public void setVisitDuration(Integer visitDuration) { 
        this.visitDuration = visitDuration;
        this.visitDurationSnake = visitDuration; // Keep both in sync
    }
    
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { 
        this.openingHours = openingHours;
        this.openingHoursSnake = openingHours; // Keep both in sync
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getAccessibility() { return accessibility; }
    public void setAccessibility(Boolean accessibility) { this.accessibility = accessibility; }
    
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { 
        this.providerId = providerId;
        this.providerIdSnake = providerId; // Keep both in sync
    }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { 
        this.providerName = providerName;
        this.providerNameSnake = providerName; // Keep both in sync
    }
    
    // Legacy getters for backward compatibility
    public Integer getVisitDurationSnake() { return visitDurationSnake; }
    public String getOpeningHoursSnake() { return openingHoursSnake; }
    public Long getProviderIdSnake() { return providerIdSnake; }
    public String getProviderNameSnake() { return providerNameSnake; }
    
    public Integer getPriceLevel() { return priceLevel; }
    public void setPriceLevel(Integer priceLevel) { this.priceLevel = priceLevel; }
    
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }

    @Override
    public String toString() {
        return "POI{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", categories='" + Arrays.toString(categories) + '\'' +
                ", rating=" + rating +
                '}';
    }
}