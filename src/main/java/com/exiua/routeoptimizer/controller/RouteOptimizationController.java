package com.exiua.routeoptimizer.controller;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.model.POI;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.service.MockDataService;
import com.exiua.routeoptimizer.service.RouteOptimizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller implementing Request-Response with Status Polling pattern
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Route Optimization", description = "API for route optimization using async processing")
public class RouteOptimizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizationController.class);
    
    private final RouteOptimizationService optimizationService;
    private final MockDataService mockDataService;
    private final Random random = new Random();
    
    public RouteOptimizationController(RouteOptimizationService optimizationService, 
                                     MockDataService mockDataService) {
        this.optimizationService = optimizationService;
        this.mockDataService = mockDataService;
    }

    /**
     * Submit route optimization request (returns 202 Accepted)
     */
    @PostMapping("/routes/optimize")
    @Operation(summary = "Submit route optimization request", 
               description = "Submits a route optimization request for async processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Request accepted for processing"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<JobSubmissionResponseDTO> optimizeRoute(
            @Valid @RequestBody RouteOptimizationRequest request) {
        
        logger.info("Received route optimization request for {} POIs", 
                   request.getPois() != null ? request.getPois().size() : 0);
        
        try {
            // If no route ID provided, generate one randomly from mock data
            if (request.getRouteId() == null || request.getRouteId().isEmpty()) {
                String[] routeTypes = {"adventure", "cultural", "beach", "nature"};
                String randomRouteType = routeTypes[random.nextInt(routeTypes.length)];
                request.setRouteId("route-" + randomRouteType + "-" + System.currentTimeMillis());
                
                // If no POIs provided, get mock POIs for the route type
                if (request.getPois() == null || request.getPois().isEmpty()) {
                    List<POI> mockPOIs = mockDataService.getPOIsForRoute(randomRouteType);
                    request.setPois(mockPOIs);
                    logger.info("Generated {} mock POIs for route type: {}", mockPOIs.size(), randomRouteType);
                }
            }
            
            // Submit the optimization request
            JobSubmissionResponseDTO response = optimizationService.submitOptimizationRequest(request);
            
            // Add Retry-After header as per the pattern
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", response.getRetryAfterSeconds().toString());
            headers.add("Location", response.getPollingUrl());
            
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .headers(headers)
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Error processing optimization request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get job status for polling
     */
    @GetMapping("/jobs/{jobId}/status")
    @Operation(summary = "Get job status", 
               description = "Polls for the status of a route optimization job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job completed successfully"),
        @ApiResponse(responseCode = "202", description = "Job still processing"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "500", description = "Job failed")
    })
    public ResponseEntity<JobStatusResponseDTO> getJobStatus(
            @Parameter(description = "Job ID to check status for")
            @PathVariable String jobId) {
        
        logger.debug("Polling status for job: {}", jobId);
        
        Optional<JobStatusResponseDTO> statusOpt = optimizationService.getJobStatus(jobId);
        
        if (statusOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        JobStatusResponseDTO status = statusOpt.get();
        HttpHeaders headers = new HttpHeaders();
        
        // Return appropriate HTTP status based on job status
        return switch (status.getStatus()) {
            case "PENDING", "PROCESSING" -> {
                headers.add("Retry-After", status.getRetryAfterSeconds().toString());
                yield ResponseEntity.status(HttpStatus.ACCEPTED)
                        .headers(headers)
                        .body(status);
            }
            case "COMPLETED" -> ResponseEntity.ok(status);
            case "FAILED" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
            case "CANCELLED" -> ResponseEntity.status(HttpStatus.GONE).body(status);
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
        };
    }

    /**
     * Cancel a job
     */
    @DeleteMapping("/jobs/{jobId}")
    @Operation(summary = "Cancel job", description = "Cancels a route optimization job")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Job cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "409", description = "Job cannot be cancelled (already completed)")
    })
    public ResponseEntity<Void> cancelJob(
            @Parameter(description = "Job ID to cancel")
            @PathVariable String jobId) {
        
        logger.info("Cancellation request for job: {}", jobId);
        
        boolean cancelled = optimizationService.cancelJob(jobId);
        
        if (cancelled) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get mock POIs for testing (helper endpoint)
     */
    @GetMapping("/pois/mock")
    @Operation(summary = "Get mock POIs", description = "Returns mock POI data for testing")
    public ResponseEntity<List<POI>> getMockPOIs(
            @Parameter(description = "Route type (adventure, cultural, beach, nature)")
            @RequestParam(required = false, defaultValue = "random") String routeType,
            @Parameter(description = "Number of POIs to return")
            @RequestParam(required = false, defaultValue = "8") int count) {
        
        List<POI> pois;
        if ("random".equals(routeType)) {
            pois = mockDataService.getRandomPOISubset(count);
        } else {
            pois = mockDataService.getPOIsForRoute(routeType);
        }
        
        return ResponseEntity.ok(pois);
    }

    /**
     * Get all available mock POIs
     */
    @GetMapping("/pois/all")
    @Operation(summary = "Get all mock POIs", description = "Returns all available mock POI data")
    public ResponseEntity<List<POI>> getAllMockPOIs() {
        List<POI> pois = mockDataService.getAllMockPOIs();
        return ResponseEntity.ok(pois);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<HealthResponse> healthCheck() {
        HealthResponse health = new HealthResponse(
            "UP", 
            "Route Optimizer Service", 
            java.time.LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(health);
    }
    
    /**
     * Health response record
     */
    public record HealthResponse(String status, String service, String timestamp) {}
}