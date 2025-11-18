package com.exiua.routeoptimizer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class EnrichedProcessingPOI {
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
    private List<String> categories;

}