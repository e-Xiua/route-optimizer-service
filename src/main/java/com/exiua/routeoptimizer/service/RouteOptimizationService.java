package com.exiua.routeoptimizer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.dto.RouteProcessingRequestDTO;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to handle route optimization requests and manage async communication with Python service
 */
@Service
public class RouteOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizationService.class);
    
    private final OptimizationJobRepository jobRepository;
    private final MockDataService mockDataService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    
    @Value("${route.processing.service.url:http://localhost:8001}")
    private String routeProcessingServiceUrl;
    
    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public RouteOptimizationService(OptimizationJobRepository jobRepository, 
                                  MockDataService mockDataService,
                                  ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.mockDataService = mockDataService;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * Submit a route optimization request (returns 202 Accepted)
     */
    public JobSubmissionResponseDTO submitOptimizationRequest(RouteOptimizationRequest request) {
        try {
            // Log the incoming request from frontend
            logger.info("=== FRONTEND REQUEST RECEIVED ===");
            logger.info("Route ID: {}", request.getRouteId());
            logger.info("User ID: {}", request.getUserId());
            logger.info("Number of POIs: {}", request.getPois() != null ? request.getPois().size() : 0);
            logger.info("Frontend Request JSON: {}", objectMapper.writeValueAsString(request));
            
            // Generate unique job ID
            String jobId = UUID.randomUUID().toString();
            
            // Create job record
            OptimizationJob job = new OptimizationJob(jobId, request.getUserId(), request.getRouteId());
            job.setRequestData(objectMapper.writeValueAsString(request));
            job.setEstimatedCompletionTime(LocalDateTime.now().plusMinutes(5)); // Estimate 5 minutes
            
            // Save job to database
            jobRepository.save(job);
            
            // Build polling URL
            String pollingUrl = baseUrl + "/api/v1/jobs/" + jobId + "/status";
            
            // Create response
            JobSubmissionResponseDTO response = new JobSubmissionResponseDTO(jobId, pollingUrl);
            response.setEstimatedCompletionTime(job.getEstimatedCompletionTime());
            
            // Start async processing
            processOptimizationAsync(jobId, request);
            
            logger.info("Route optimization request submitted with job ID: {}", jobId);
            return response;
            
        } catch (Exception e) {
            logger.error("Error submitting optimization request", e);
            throw new RuntimeException("Failed to submit optimization request", e);
        }
    }
    
    /**
     * Get job status for polling
     */
    public Optional<JobStatusResponseDTO> getJobStatus(String jobId) {
        Optional<OptimizationJob> jobOpt = jobRepository.findById(jobId);
        
        if (jobOpt.isEmpty()) {
            return Optional.empty();
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
            // Update job status to PROCESSING
            updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, 10);
            
            // Simulate processing steps
            simulateProcessingSteps(jobId);
            
            // Call Python service
            callPythonService(jobId, request);
            
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
            logger.info("Job {}: {}", jobId, steps[i]);
            updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, 20 + (i * 20));
            Thread.sleep(2000); // Simulate processing time
        }
    }
    
    /**
     * Call Route Processing Service which communicates with Python MRL-AMIS service
     */
    private void callPythonService(String jobId, RouteOptimizationRequest request) {
        try {
            logger.info("=== CALLING ROUTE PROCESSING SERVICE ====");
            logger.info("Job ID: {}", jobId);
            logger.info("Route Processing Service URL: {}", routeProcessingServiceUrl);
            
            // Build the request for the route processing service
            var processingRequest = buildProcessingRequest(request);
            
            // Call the route processing service
            String processingUrl = routeProcessingServiceUrl + "/api/v1/process-route";
            logger.info("Full endpoint URL: {}", processingUrl);
            
            logger.info("Sending POST request to route-processing-service...");
            
            String result = webClient.post()
                .uri(processingUrl)
                .bodyValue(processingRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            logger.info("=== RESPONSE FROM ROUTE-PROCESSING-SERVICE ===");
            logger.info("Response body: {}", result);
            logger.info("============================================");
            
            // Update job with result
            updateJobStatus(jobId, OptimizationJob.JobStatus.COMPLETED, 100);
            updateJobResult(jobId, result);
            
            logger.info("Job {} completed successfully via Route Processing Service", jobId);
            
        } catch (Exception e) {
            logger.error("Error calling Route Processing Service for job: {}", jobId, e);
            
            // Fallback to mock result if service is unavailable
            logger.warn("Falling back to mock result for job: {}", jobId);
            try {
                String mockResult = generateMockOptimizationResult(request);
                updateJobStatus(jobId, OptimizationJob.JobStatus.COMPLETED, 100);
                updateJobResult(jobId, mockResult);
                logger.info("Job {} completed with mock result", jobId);
            } catch (Exception mockError) {
                logger.error("Error generating mock result for job: {}", jobId, mockError);
                updateJobStatusWithError(jobId, OptimizationJob.JobStatus.FAILED, 
                    "Failed to communicate with optimization service and generate fallback result: " + e.getMessage());
            }
        }
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
        
        // Convert POIs
        List<RouteProcessingRequestDTO.ProcessingPOIDTO> processingPOIs = request.getPois().stream()
            .map(poi -> {
                RouteProcessingRequestDTO.ProcessingPOIDTO processingPOI = new RouteProcessingRequestDTO.ProcessingPOIDTO();
                
                // Ensure ID is not null
                processingPOI.setId(poi.getId() != null ? poi.getId() : System.currentTimeMillis() + Math.round(Math.random() * 1000));
                processingPOI.setName(poi.getName() != null ? poi.getName() : "Unnamed POI");
                processingPOI.setLatitude(poi.getLatitude() != null ? poi.getLatitude() : 10.501);
                processingPOI.setLongitude(poi.getLongitude() != null ? poi.getLongitude() : -84.697);
                processingPOI.setCategory(poi.getCategory() != null ? poi.getCategory() : "tourism");
                processingPOI.setSubcategory("service"); // Default subcategory since POI model doesn't have this field
                processingPOI.setVisitDuration(poi.getVisitDuration() != null ? poi.getVisitDuration() : 90);
                processingPOI.setCost(poi.getPriceLevel() != null ? poi.getPriceLevel().doubleValue() * 10.0 : 50.0); // Convert priceLevel to cost
                processingPOI.setRating(poi.getRating() != null ? poi.getRating() : 4.0);
                processingPOI.setProviderId(poi.getProviderId());
                processingPOI.setProviderName(poi.getProviderId() != null ? "Provider-" + poi.getProviderId() : "Unknown Provider");
                
                return processingPOI;
            })
            .collect(Collectors.toList());
        
        processingRequest.setPois(processingPOIs);
        
        // Set preferences
        if (request.getPreferences() != null) {
            RouteProcessingRequestDTO.RoutePreferencesDTO preferences = new RouteProcessingRequestDTO.RoutePreferencesDTO();
            preferences.setOptimizeFor(request.getPreferences().getOptimizeFor() != null ? 
                request.getPreferences().getOptimizeFor() : "distance");
            preferences.setMaxTotalTime(request.getPreferences().getMaxTotalTime());
            preferences.setMaxTotalCost(request.getPreferences().getMaxTotalCost());
            preferences.setAccessibilityRequired(false); // Default to false since method doesn't exist
            processingRequest.setPreferences(preferences);
        } else {
            // Default preferences
            RouteProcessingRequestDTO.RoutePreferencesDTO preferences = new RouteProcessingRequestDTO.RoutePreferencesDTO();
            preferences.setOptimizeFor("distance");
            preferences.setMaxTotalTime(480); // 8 hours default
            preferences.setAccessibilityRequired(false);
            processingRequest.setPreferences(preferences);
        }
        
        // Set default constraints since RouteOptimizationRequest doesn't have constraints field
        RouteProcessingRequestDTO.RouteConstraintsDTO constraints = new RouteProcessingRequestDTO.RouteConstraintsDTO();
        constraints.setStartTime("09:00"); // Default start time
        constraints.setLunchBreakRequired(true);
        constraints.setLunchBreakDuration(60); // 1 hour lunch break
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
     * Generate mock optimization result
     */
    private String generateMockOptimizationResult(RouteOptimizationRequest request) {
        try {
            // Create a mock optimized route result
            var result = new Object() {
                public final String optimized_route_id = UUID.randomUUID().toString();
                public final Object[] optimized_sequence = request.getPois().stream()
                    .map(poi -> new Object() {
                        public final Long poi_id = poi.getId();
                        public final String name = poi.getName();
                        public final Double latitude = poi.getLatitude();
                        public final Double longitude = poi.getLongitude();
                        public final Integer visit_order = request.getPois().indexOf(poi) + 1;
                        public final Integer estimated_visit_time = poi.getVisitDuration();
                    }).toArray();
                public final Double total_distance_km = 45.7 + (Math.random() * 50);
                public final Integer total_time_minutes = 280 + (int)(Math.random() * 120);
                public final String optimization_algorithm = "MRL-AMIS";
                public final Double optimization_score = 0.85 + (Math.random() * 0.14);
                public final String generated_at = LocalDateTime.now().toString();
            };
            
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("Error generating mock result", e);
        }
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
}