package com.exiua.routeoptimizer.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.service.EnhancedRouteOptimizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller implementing Request-Response with Status Polling pattern
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Route Optimization", description = "API for route optimization using async processing")
public class RouteJobStatusManagementController {
       
    private final EnhancedRouteOptimizationService enhancedOptimizationService;
    

    public RouteJobStatusManagementController(EnhancedRouteOptimizationService enhancedOptimizationService) {
        this.enhancedOptimizationService = enhancedOptimizationService;
        
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

        Optional<JobStatusResponseDTO> jobStatusOpt = enhancedOptimizationService.getJobStatus(jobId);

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

        boolean cancelled = enhancedOptimizationService.cancelJob(jobId);

        if (cancelled) {
            return ResponseEntity.noContent().build();
        } else {
            // This could be because the job was not found or already completed
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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