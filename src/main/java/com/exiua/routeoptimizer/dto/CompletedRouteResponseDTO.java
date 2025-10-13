package com.exiua.routeoptimizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class CompletedRouteResponseDTO {

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("optimizedRouteId")
    private String optimizedRouteId;

    @JsonProperty("optimizedSequence")
    private List<OptimizedPOIDTO> optimizedSequence;

    @JsonProperty("totalDistanceKm")
    private Double totalDistanceKm;

    @JsonProperty("totalTimeMinutes")
    private Integer totalTimeMinutes;

    @JsonProperty("optimizationAlgorithm")
    private String optimizationAlgorithm;

    @JsonProperty("optimizationScore")
    private Double optimizationScore;

    @JsonProperty("generatedAt")
    private LocalDateTime generatedAt;

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOptimizedRouteId() {
        return optimizedRouteId;
    }

    public void setOptimizedRouteId(String optimizedRouteId) {
        this.optimizedRouteId = optimizedRouteId;
    }

    public List<OptimizedPOIDTO> getOptimizedSequence() {
        return optimizedSequence;
    }

    public void setOptimizedSequence(List<OptimizedPOIDTO> optimizedSequence) {
        this.optimizedSequence = optimizedSequence;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public Integer getTotalTimeMinutes() {
        return totalTimeMinutes;
    }

    public void setTotalTimeMinutes(Integer totalTimeMinutes) {
        this.totalTimeMinutes = totalTimeMinutes;
    }

    public String getOptimizationAlgorithm() {
        return optimizationAlgorithm;
    }

    public void setOptimizationAlgorithm(String optimizationAlgorithm) {
        this.optimizationAlgorithm = optimizationAlgorithm;
    }

    public Double getOptimizationScore() {
        return optimizationScore;
    }

    public void setOptimizationScore(Double optimizationScore) {
        this.optimizationScore = optimizationScore;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public static class OptimizedPOIDTO {
        @JsonProperty("poiId")
        private Long poiId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("visitOrder")
        private Integer visitOrder;

        @JsonProperty("estimatedVisitTime")
        private Integer estimatedVisitTime;

        @JsonProperty("arrivalTime")
        private String arrivalTime;

        @JsonProperty("departureTime")
        private String departureTime;

        // Getters and Setters
        public Long getPoiId() {
            return poiId;
        }

        public void setPoiId(Long poiId) {
            this.poiId = poiId;
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

        public Integer getVisitOrder() {
            return visitOrder;
        }

        public void setVisitOrder(Integer visitOrder) {
            this.visitOrder = visitOrder;
        }

        public Integer getEstimatedVisitTime() {
            return estimatedVisitTime;
        }

        public void setEstimatedVisitTime(Integer estimatedVisitTime) {
            this.estimatedVisitTime = estimatedVisitTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(String arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(String departureTime) {
            this.departureTime = departureTime;
        }
    }
}
