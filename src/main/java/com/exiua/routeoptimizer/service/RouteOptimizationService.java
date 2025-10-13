package com.exiua.routeoptimizer.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exiua.routeoptimizer.dto.CompletedRouteResponseDTO;
import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.dto.RouteProcessingRequestDTO;
import com.exiua.routeoptimizer.exceptions.JobNotFoundException;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to handle route optimization requests and manage async communication with Python service
 */
@Service
public class RouteOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizationService.class);
    
    private final OptimizationJobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private RouteOptimizationService self;
    
    @Value("${route.processing.service.url:http://localhost:8001}")
    private String routeProcessingServiceUrl;
    
    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public RouteOptimizationService(OptimizationJobRepository jobRepository, 
                                  ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    @Autowired
    public void setSelf(@Lazy RouteOptimizationService self) {
        this.self = self;
    }
    
    /**
     * Submit a route optimization request (returns 202 Accepted)
     */
    public JobSubmissionResponseDTO submitOptimizationRequest(RouteOptimizationRequest request) {
        try {
            String jobId = "job-" + UUID.randomUUID().toString();
            logger.info("Received route optimization request. Assigned Job ID: {}", jobId);

            // Create and save initial job status
            OptimizationJob job = new OptimizationJob(jobId, request.getUserId(), request.getRouteId());
            job.setRequestData(objectMapper.writeValueAsString(request));
            job.setStatus(OptimizationJob.JobStatus.PENDING);
            job.setRouteId(request.getRouteId());
            jobRepository.save(job);

            // Log the request details for debugging
            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Frontend Request JSON: {}", objectMapper.writeValueAsString(request));
                } catch (JsonProcessingException e) {
                    logger.warn("Could not serialize request for logging", e);
                }
            }

            // Start async processing
            self.processOptimizationAsync(jobId, request);

            // Create response DTO
            JobSubmissionResponseDTO response = new JobSubmissionResponseDTO();
            response.setJobId(jobId);
            response.setStatusUrl(baseUrl + "/api/v1/jobs/" + jobId + "/status");
            response.setCancelUrl(baseUrl + "/api/v1/jobs/" + jobId);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to submit optimization request", e);
            throw new RuntimeException("Failed to submit optimization request", e);
        }
    }
    
    /**
     * Get job status for polling
     */
    public Optional<JobStatusResponseDTO> getJobStatus(String jobId) {
        Optional<OptimizationJob> jobOpt = jobRepository.findById(jobId);
        
        if (jobOpt.isEmpty()) {
            throw new JobNotFoundException("No job found with ID: " + jobId);
        }
        
        OptimizationJob job = jobOpt.get();
        JobStatusResponseDTO response = new JobStatusResponseDTO(jobId, job.getStatus().name());
        
        response.setProgressPercentage(job.getProgressPercentage());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setEstimatedCompletionTime(job.getEstimatedCompletionTime());
        
        // Set appropriate message and retry interval based on status
        switch (job.getStatus()) {
            case PENDING:
                response.setMessage("Request is queued for processing");
                response.setRetryAfterSeconds(30);
                break;
            case PROCESSING:
                response.setMessage("Route optimization is in progress");
                response.setRetryAfterSeconds(20);
                break;
            case COMPLETED:
                response.setMessage("Route optimization completed successfully");
                try {
                    if (job.getResultData() != null) {
                        response.setResult(objectMapper.readValue(job.getResultData(), Object.class));
                    }
                } catch (Exception e) {
                    logger.error("Error parsing result data for job {}", jobId, e);
                }
                break;
            case FAILED:
                response.setMessage("Route optimization failed");
                response.setError(new JobStatusResponseDTO.ErrorDetails(
                    "OPTIMIZATION_FAILED", 
                    job.getErrorMessage() != null ? job.getErrorMessage() : "Unknown error",
                    "The route optimization process encountered an error"
                ));
                break;
            case CANCELLED:
                response.setMessage("Route optimization was cancelled");
                break;
        }
        
        return Optional.of(response);
    }
    
    /**
     * Process optimization request asynchronously
     */
    @Async
    public void processOptimizationAsync(String jobId, RouteOptimizationRequest request) {
        logger.info("Starting async processing for job: {}", jobId);
        
        try {
            // Simulate processing steps
            simulateProcessingSteps(jobId);
            
            // Call Python service
            callPythonService(jobId, request);

        } catch (InterruptedException e) {
            logger.warn("Job {} was interrupted", jobId);
            updateJobStatusWithError(jobId, OptimizationJob.JobStatus.FAILED, "Processing was interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error during async processing for job: {}", jobId, e);
            updateJobStatusWithError(jobId, OptimizationJob.JobStatus.FAILED, e.getMessage());
        }
    }
    
    /**
     * Simulate processing steps with progress updates
     */
    private void simulateProcessingSteps(String jobId) throws InterruptedException {
        String[] steps = {
            "Validating POI data",
            "Calculating distances",
            "Optimizing route",
            "Generating result"
        };
        
        for (int i = 0; i < steps.length; i++) {
            Thread.sleep(1000); // Simulate work
            updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, (i + 1) * (100 / steps.length));
        }
    }
    
    /**
     * Call Route Processing Service which communicates with Python MRL-AMIS service
     */
    private void callPythonService(String jobId, RouteOptimizationRequest request) {
        
        RouteProcessingRequestDTO processingRequest = buildProcessingRequest(request);
        
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling route processing service at URL: {}", routeProcessingServiceUrl + "/api/v1/process-route");
                logger.debug("Request body for processing service: {}", objectMapper.writeValueAsString(processingRequest));
            }
        } catch (JsonProcessingException e) {
            logger.warn("Could not serialize processing request for logging", e);
        }

        webClient.post()
            .uri(routeProcessingServiceUrl + "/api/v1/process-route")
            .bodyValue(processingRequest)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> {
                logger.info("Successfully received response from processing service for job: {}", jobId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Response from processing service: {}", response);
                }
                updateJobResult(jobId, response);
                updateJobStatus(jobId, OptimizationJob.JobStatus.COMPLETED, 100);
            })
            .doOnError(error -> {
                logger.error("Error calling processing service for job: {}", jobId, error);
                updateJobStatusWithError(jobId, OptimizationJob.JobStatus.FAILED, "Failed to call processing service: " + error.getMessage());
            })
            .subscribe();
    }
    
    /**
     * Build processing request for the route processing service
     */
    private RouteProcessingRequestDTO buildProcessingRequest(RouteOptimizationRequest request) {
        logger.info("=== BUILDING REQUEST FOR ROUTE-PROCESSING-SERVICE ===");
        
        RouteProcessingRequestDTO processingRequest = new RouteProcessingRequestDTO();
        
        // Set basic fields - ensure routeId is not null
        String routeId = request.getRouteId() != null ? request.getRouteId() : UUID.randomUUID().toString();
        String userId = request.getUserId() != null ? request.getUserId() : "system-user";
        
        processingRequest.setRouteId(routeId);
        processingRequest.setUserId(userId);
        
        logger.info("Processing Request - Route ID: {}, User ID: {}", routeId, userId);
        
        // Convert POIs - NOW SUPPORTS ALL FRONTEND FIELDS
        List<RouteProcessingRequestDTO.ProcessingPOIDTO> processingPOIs = request.getPois().stream()
            .map(poi -> {
                RouteProcessingRequestDTO.ProcessingPOIDTO processingPOI = new RouteProcessingRequestDTO.ProcessingPOIDTO();
                
                // Ensure ID is not null
                processingPOI.setId(poi.getId() != null ? poi.getId() : System.currentTimeMillis() + Math.round(Math.random() * 1000));
                processingPOI.setName(poi.getName() != null ? poi.getName() : "Unnamed POI");
                processingPOI.setLatitude(poi.getLatitude() != null ? poi.getLatitude() : 10.501);
                processingPOI.setLongitude(poi.getLongitude() != null ? poi.getLongitude() : -84.697);
                processingPOI.setCategory(poi.getCategory() != null ? poi.getCategory() : "tourism");
                processingPOI.setSubcategory(poi.getSubcategory() != null ? poi.getSubcategory() : "service");
                processingPOI.setVisitDuration(poi.getVisitDuration() != null ? poi.getVisitDuration() : 90);
                processingPOI.setCost(poi.getCost() != null ? poi.getCost() : 
                    (poi.getPriceLevel() != null ? poi.getPriceLevel().doubleValue() * 10.0 : 50.0));
                processingPOI.setRating(poi.getRating() != null ? poi.getRating() : 4.0);
                processingPOI.setProviderId(poi.getProviderId());
                processingPOI.setProviderName(poi.getProviderName() != null ? poi.getProviderName() : 
                    (poi.getProviderId() != null ? "Provider-" + poi.getProviderId() : "Unknown Provider"));
                
                return processingPOI;
            })
            .collect(Collectors.toList());
        
        processingRequest.setPois(processingPOIs);
        
        // Set preferences - NOW SUPPORTS ALL NEW FIELDS
        if (request.getPreferences() != null) {
            RouteProcessingRequestDTO.RoutePreferencesDTO preferences = new RouteProcessingRequestDTO.RoutePreferencesDTO();
            preferences.setOptimizeFor(request.getPreferences().getOptimizeFor() != null ? 
                request.getPreferences().getOptimizeFor() : "distance");
            preferences.setMaxTotalTime(request.getPreferences().getMaxTotalTime());
            preferences.setMaxTotalCost(request.getPreferences().getMaxTotalCost());
            preferences.setPreferredCategories(request.getPreferences().getPreferredCategories());
            preferences.setAvoidCategories(request.getPreferences().getAvoidCategories());
            preferences.setGroupSize(request.getPreferences().getGroupSize());
            preferences.setTouristType(request.getPreferences().getTouristType());
            preferences.setAdventureLevel(request.getPreferences().getAdventureLevel());
            preferences.setCostSensitivity(request.getPreferences().getCostSensitivity());
            preferences.setSustainabilityMin(request.getPreferences().getSustainabilityMin());
            preferences.setMaxDistanceKm(request.getPreferences().getMaxDistanceKm());
            preferences.setAccessibilityRequired(request.getPreferences().getAccessibilityRequired() != null ? 
                request.getPreferences().getAccessibilityRequired() : false);
            processingRequest.setPreferences(preferences);
        } else {
            // Default preferences
            RouteProcessingRequestDTO.RoutePreferencesDTO preferences = new RouteProcessingRequestDTO.RoutePreferencesDTO();
            preferences.setOptimizeFor("distance");
            preferences.setMaxTotalTime(480); // 8 hours default
            preferences.setAccessibilityRequired(false);
            processingRequest.setPreferences(preferences);
        }
        
        // Set constraints - NOW SUPPORTS START/END LOCATIONS
        RouteProcessingRequestDTO.RouteConstraintsDTO constraints = new RouteProcessingRequestDTO.RouteConstraintsDTO();
        if (request.getConstraints() != null) {
            // Map start/end locations if present
            if (request.getConstraints().getStartLocation() != null) {
                RouteProcessingRequestDTO.LocationDTO startLocation = new RouteProcessingRequestDTO.LocationDTO(
                    request.getConstraints().getStartLocation().getLatitude(),
                    request.getConstraints().getStartLocation().getLongitude()
                );
                constraints.setStartLocation(startLocation);
            }
            if (request.getConstraints().getEndLocation() != null) {
                RouteProcessingRequestDTO.LocationDTO endLocation = new RouteProcessingRequestDTO.LocationDTO(
                    request.getConstraints().getEndLocation().getLatitude(),
                    request.getConstraints().getEndLocation().getLongitude()
                );
                constraints.setEndLocation(endLocation);
            }
            constraints.setStartTime(request.getConstraints().getStartTime() != null ? 
                request.getConstraints().getStartTime() : "09:00");
            constraints.setLunchBreakRequired(request.getConstraints().getLunchBreakRequired() != null ? 
                request.getConstraints().getLunchBreakRequired() : true);
            constraints.setLunchBreakDuration(request.getConstraints().getLunchBreakDuration() != null ? 
                request.getConstraints().getLunchBreakDuration() : 60);
        } else {
            // Default constraints
            constraints.setStartTime("09:00");
            constraints.setLunchBreakRequired(true);
            constraints.setLunchBreakDuration(60);
        }
        processingRequest.setConstraints(constraints);
        
        try {
            String processingRequestJson = objectMapper.writeValueAsString(processingRequest);
            logger.info("=== REQUEST TO ROUTE-PROCESSING-SERVICE ===");
            logger.info("Processing Request JSON: {}", processingRequestJson);
            logger.info("Total POIs to process: {}", processingRequest.getPois() != null ? processingRequest.getPois().size() : 0);
            logger.info("===============================================");
        } catch (Exception e) {
            logger.warn("Failed to serialize processing request for logging: {}", e.getMessage());
        }
        
        return processingRequest;
    }
        
    /**
     * Update job status and progress
     */
    private void updateJobStatus(String jobId, OptimizationJob.JobStatus status, Integer progress) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setProgressPercentage(progress);
            jobRepository.save(job);
        });
    }
    
    /**
     * Update job status with error
     */
    private void updateJobStatusWithError(String jobId, OptimizationJob.JobStatus status, String errorMessage) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setErrorMessage(errorMessage);
            jobRepository.save(job);
        });
    }
    
    /**
     * Update job with result data
     */
    private void updateJobResult(String jobId, String resultData) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setResultData(resultData);
            jobRepository.save(job);
        });
    }
    
    /**
     * Cancel a job
     */
    public boolean cancelJob(String jobId) {
        Optional<OptimizationJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            OptimizationJob job = jobOpt.get();
            if (job.getStatus() == OptimizationJob.JobStatus.PENDING || 
                job.getStatus() == OptimizationJob.JobStatus.PROCESSING) {
                job.setStatus(OptimizationJob.JobStatus.CANCELLED);
                jobRepository.save(job);
                return true;
            }
        }
        return false;
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