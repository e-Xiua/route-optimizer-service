package com.exiua.routeoptimizer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.dto.CompletedRouteResponseDTO;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
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
                .collect(Collectors.toList());
    }
    
    /**
     * Get completed routes for a specific user
     */
    public List<CompletedRouteResponseDTO> getCompletedRoutesByUser(String userId) {
        logger.info("Fetching completed routes for user: {}", userId);
        return jobRepository.findByUserIdAndStatusOrderByCompletedAtDesc(userId, OptimizationJob.JobStatus.COMPLETED)
                .stream()
                .map(this::mapToCompletedRouteResponse)
                .collect(Collectors.toList());
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
                OptimizationResultData resultData = objectMapper.readValue(job.getResultData(), OptimizationResultData.class);
                response.setOptimizedRouteId(resultData.optimizedRouteId);
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
                        }).collect(Collectors.toList());
                    response.setOptimizedSequence(poiDTOs);
                }
            } catch (Exception e) {
                logger.error("Failed to parse result data for job: {}", job.getJobId(), e);
            }
        }

        return response;
    }

        // Inner classes for deserializing result_data
    private static class OptimizationResultData {
        @JsonProperty("optimizedRouteId")
        String optimizedRouteId;
        @JsonProperty("optimizedSequence")
        List<OptimizedPOIData> optimizedSequence;
        @JsonProperty("totalDistanceKm")
        Double totalDistanceKm;
        @JsonProperty("totalTimeMinutes")
        Integer totalTimeMinutes;
        @JsonProperty("algorithm")
        String algorithm;
        @JsonProperty("optimizationScore")
        Double optimizationScore;
        @JsonProperty("generatedAt")
        String generatedAt;
    }

    private static class OptimizedPOIData {
        @JsonProperty("poiId")
        Long poiId;
        @JsonProperty("name")
        String name;
        @JsonProperty("latitude")
        Double latitude;
        @JsonProperty("longitude")
        Double longitude;
        @JsonProperty("visitOrder")
        Integer visitOrder;
        @JsonProperty("estimatedVisitTime")
        Integer estimatedVisitTime;
        @JsonProperty("arrivalTime")
        String arrivalTime;
        @JsonProperty("departureTime")
        String departureTime;
    }

}
