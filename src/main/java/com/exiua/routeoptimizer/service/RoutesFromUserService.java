package com.exiua.routeoptimizer.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.dto.CompletedRouteResponseDTO;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RoutesFromUserService {

    private static final Logger logger = LoggerFactory.getLogger(RoutesFromUserService.class);

    private final OptimizationJobRepository jobRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoutesFromUserService(OptimizationJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

     /**
     * Get all completed route optimizations
     */
    public List<CompletedRouteResponseDTO> getCompletedRoutes() {
        logger.info("Fetching all completed routes");
        return jobRepository.findByStatusOrderByCompletedAtDesc(OptimizationJob.JobStatus.COMPLETED)
                .stream()
                .map(this::mapToCompletedRouteResponse)
                .toList();
    }
    
    /**
     * Get completed routes for a specific user
     */
    public List<CompletedRouteResponseDTO> getCompletedRoutesByUser(String userId) {
        logger.info("Fetching completed routes for user: {}", userId);
        return jobRepository.findByUserIdAndStatusOrderByCompletedAtDesc(userId, OptimizationJob.JobStatus.COMPLETED)
                .stream()
                .map(this::mapToCompletedRouteResponse)
                .toList();
    }

        /**
     * Map OptimizationJob to CompletedRouteResponseDTO
     */
    private CompletedRouteResponseDTO mapToCompletedRouteResponse(OptimizationJob job) {
        CompletedRouteResponseDTO response = new CompletedRouteResponseDTO();
        response.setRequestId(job.getJobId());
        response.setGeneratedAt(job.getCompletedAt());

        if (job.getResultData() != null && !job.getResultData().isEmpty()) {
            try {
                logger.debug("Parsing result data for job: {}", job.getJobId());
                logger.debug("Raw result data: {}", job.getResultData());
                
                OptimizationResultData resultData = objectMapper.readValue(job.getResultData(), OptimizationResultData.class);
                
                logger.debug("Parsed result - requestId: {}, routeId: {}, distance: {}, time: {}", 
                    resultData.requestId, resultData.optimizedRouteId, resultData.totalDistanceKm, resultData.totalTimeMinutes);
                
                // Usar requestId si optimizedRouteId es null
                String finalRouteId = resultData.optimizedRouteId != null ? resultData.optimizedRouteId : resultData.requestId;
                response.setOptimizedRouteId(finalRouteId);
                response.setTotalDistanceKm(resultData.totalDistanceKm);
                response.setTotalTimeMinutes(resultData.totalTimeMinutes);
                response.setOptimizationAlgorithm(resultData.algorithm);
                response.setOptimizationScore(resultData.optimizationScore);

                if (resultData.optimizedSequence != null) {
                    List<CompletedRouteResponseDTO.OptimizedPOIDTO> poiDTOs = resultData.optimizedSequence.stream()
                        .map(poiData -> {
                            CompletedRouteResponseDTO.OptimizedPOIDTO poiDTO = new CompletedRouteResponseDTO.OptimizedPOIDTO();
                            poiDTO.setPoiId(poiData.poiId);
                            poiDTO.setName(poiData.name);
                            poiDTO.setLatitude(poiData.latitude);
                            poiDTO.setLongitude(poiData.longitude);
                            poiDTO.setVisitOrder(poiData.visitOrder);
                            poiDTO.setEstimatedVisitTime(poiData.estimatedVisitTime);
                            poiDTO.setArrivalTime(poiData.arrivalTime);
                            poiDTO.setDepartureTime(poiData.departureTime);
                            return poiDTO;
                        }).toList();
                    response.setOptimizedSequence(poiDTOs);
                }
            } catch (Exception e) {
                logger.error("Failed to parse result data for job: {}", job.getJobId(), e);
            }
        }

        return response;
    }

        // Inner classes for deserializing result_data
    // NOTA: Estos campos deben coincidir con el formato del JSON guardado (tanto camelCase como snake_case)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OptimizationResultData {
        @JsonProperty("requestId")
        String requestId;
        @JsonProperty("optimizedRouteId")
        @JsonAlias({"optimized_route_id"})
        String optimizedRouteId;
        @JsonProperty("optimizedSequence")
        @JsonAlias({"optimized_sequence"})
        List<OptimizedPOIData> optimizedSequence;
        @JsonProperty("totalDistanceKm")
        @JsonAlias({"total_distance_km"})
        Double totalDistanceKm;
        @JsonProperty("totalTimeMinutes")
        @JsonAlias({"total_time_minutes"})
        Integer totalTimeMinutes;
        @JsonProperty("algorithm")
        @JsonAlias({"optimization_algorithm"})
        String algorithm;
        @JsonProperty("optimizationScore")
        @JsonAlias({"optimization_score"})
        Double optimizationScore;
        @JsonProperty("generatedAt")
        @JsonAlias({"generated_at", "processed_at", "processedAt"})
        String generatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OptimizedPOIData {
        @JsonProperty("poiId")
        @JsonAlias({"poi_id"})
        Long poiId;
        @JsonProperty("name")
        String name;
        @JsonProperty("latitude")
        Double latitude;
        @JsonProperty("longitude")
        Double longitude;
        @JsonProperty("visitOrder")
        @JsonAlias({"visit_order"})
        Integer visitOrder;
        @JsonProperty("estimatedVisitTime")
        @JsonAlias({"estimated_visit_time"})
        Integer estimatedVisitTime;
        @JsonProperty("arrivalTime")
        @JsonAlias({"arrival_time"})
        String arrivalTime;
        @JsonProperty("departureTime")
        @JsonAlias({"departure_time"})
        String departureTime;
    }

}
