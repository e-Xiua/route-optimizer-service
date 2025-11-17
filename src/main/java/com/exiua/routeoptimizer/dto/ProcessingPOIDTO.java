package com.exiua.routeoptimizer.dto;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingPOIDTO {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String category;
    private String subcategory;
    private Integer visitDuration;
    private Double cost;
    private Double rating;
    private String openingHours;
    private String description;
    private String imageUrl;
    private Boolean accessibility;
    private Long providerId;
    private String providerName;
    private List<String> categories; // Using List instead of String[] for consistency

    // Helper method to convert to String[] if needed for external APIs
    public String[] getCategoriesAsArray() {
        if (categories == null) return new String[0];
        return categories.toArray(new String[0]);
    }

    // Helper method to set categories from array
    public void setCategoriesFromArray(String[] categoriesArray) {
        if (categoriesArray == null) {
            this.categories = null;
        } else {
            this.categories = Arrays.asList(categoriesArray);
        }
    }

}