package com.exiua.routeoptimizer.model;

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
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("rating")
    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating;
    
    @JsonProperty("visit_duration")
    private Integer visitDuration; // in minutes
    
    @JsonProperty("opening_hours")
    private String openingHours;
    
    @JsonProperty("price_level")
    private Integer priceLevel; // 1-4 scale
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("provider_id")
    private Long providerId;
    
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
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getVisitDuration() { return visitDuration; }
    public void setVisitDuration(Integer visitDuration) { this.visitDuration = visitDuration; }
    
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    
    public Integer getPriceLevel() { return priceLevel; }
    public void setPriceLevel(Integer priceLevel) { this.priceLevel = priceLevel; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }

    @Override
    public String toString() {
        return "POI{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", category='" + category + '\'' +
                ", rating=" + rating +
                '}';
    }
}