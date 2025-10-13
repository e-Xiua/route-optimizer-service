package com.exiua.routeoptimizer.controller;

import java.util.List;
import java.util.Optional;

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

import com.exiua.routeoptimizer.dto.CompletedRouteResponseDTO;
import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
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
    
    public RouteOptimizationController(RouteOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
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
        
        // If POIs are not provided, generate mock data
        if (request.getPois() == null || request.getPois().isEmpty()) {
            logger.info("No POIs provided, mock data will be generated.");
            // This part is removed as MockDataService is no longer injected
        }
        
        JobSubmissionResponseDTO response = optimizationService.submitOptimizationRequest(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", response.getStatusUrl());
        
        return new ResponseEntity<>(response, headers, HttpStatus.ACCEPTED);
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
        
        Optional<JobStatusResponseDTO> jobStatusOpt = optimizationService.getJobStatus(jobId);
        
        if (jobStatusOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        JobStatusResponseDTO jobStatus = jobStatusOpt.get();
        
        switch (jobStatus.getStatus()) {
            case "COMPLETED":
                return ResponseEntity.ok(jobStatus);
            case "PROCESSING":
            case "PENDING":
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(jobStatus);
            case "FAILED":
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jobStatus);
            default:
                return ResponseEntity.ok(jobStatus);
        }
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
        
        boolean cancelled = optimizationService.cancelJob(jobId);
        
        if (cancelled) {
            return ResponseEntity.noContent().build();
        } else {
            // This could be because the job was not found or already completed
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Get all completed route optimizations
     */
    @GetMapping("/routes/completed")
    @Operation(summary = "Get all completed routes", 
               description = "Returns all route optimizations that have been completed successfully")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved completed routes"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CompletedRouteResponseDTO>> getCompletedRoutes(
            @Parameter(description = "Filter by user ID (optional)")
            @RequestParam(required = false) String userId) {
        
        try {
            List<CompletedRouteResponseDTO> completedRoutes;
            if (userId != null && !userId.isEmpty()) {
                logger.info("Fetching completed routes for user: {}", userId);
                completedRoutes = optimizationService.getCompletedRoutesByUser(userId);
            } else {
                logger.info("Fetching all completed routes");
                completedRoutes = optimizationService.getCompletedRoutes();
            }
            return ResponseEntity.ok(completedRoutes);
        } catch (Exception e) {
            logger.error("Error fetching completed routes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Checks the health of the service")
    public ResponseEntity<HealthResponse> healthCheck() {
        String timestamp = java.time.LocalDateTime.now().toString();
        HealthResponse response = new HealthResponse("OK", "route-optimizer-service", timestamp);
        return ResponseEntity.ok(response);
    }
    
    public record HealthResponse(String status, String service, String timestamp) {}
}