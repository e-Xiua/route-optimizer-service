package com.exiua.routeoptimizer.service;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.dto.RouteProcessingRequestDTO;
import com.exiua.routeoptimizer.exceptions.JobNotFoundException;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RouteJobManagementService {

    private static final Logger logger = LoggerFactory.getLogger(RouteJobManagementService.class);
    
    private final OptimizationJobRepository jobRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private RouteJobManagementService self;
    
    @Value("${route.processing.service.url:http://localhost:8086}")
    private String routeProcessingServiceUrl;
    
    @Value("${server.base-url:http://localhost:8085}")
    private String baseUrl;

    public RouteJobManagementService(OptimizationJobRepository jobRepository, 
                                  ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    @Autowired
    public void setSelf(@Lazy RouteJobManagementService self) {
        this.self = self;
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
