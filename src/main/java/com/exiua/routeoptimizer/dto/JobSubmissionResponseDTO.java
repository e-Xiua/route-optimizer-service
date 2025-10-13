package com.exiua.routeoptimizer.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for async job submission (202 Accepted)
 */
public class JobSubmissionResponseDTO {
    
    @JsonProperty("job_id")
    private String jobId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("polling_url")
    private String pollingUrl;
    
    @JsonProperty("estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;
    
    @JsonProperty("retry_after_seconds")
    private Integer retryAfterSeconds;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("status_url")
    private String statusUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    // Constructors
    public JobSubmissionResponseDTO() {}
    
    public JobSubmissionResponseDTO(String jobId, String pollingUrl) {
        this.jobId = jobId;
        this.status = "ACCEPTED";
        this.message = "Route optimization request accepted and is being processed";
        this.pollingUrl = pollingUrl;
        this.retryAfterSeconds = 30; // Default polling interval
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getPollingUrl() { return pollingUrl; }
    public void setPollingUrl(String pollingUrl) { this.pollingUrl = pollingUrl; }
    
    public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) { 
        this.estimatedCompletionTime = estimatedCompletionTime; 
    }
    
    public Integer getRetryAfterSeconds() { return retryAfterSeconds; }
    public void setRetryAfterSeconds(Integer retryAfterSeconds) { this.retryAfterSeconds = retryAfterSeconds; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatusUrl() { return statusUrl; }
    public void setStatusUrl(String statusUrl) { this.statusUrl = statusUrl; }

    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
}