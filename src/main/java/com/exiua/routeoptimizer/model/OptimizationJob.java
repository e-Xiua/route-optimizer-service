package com.exiua.routeoptimizer.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Entity to track optimization job status
 */
@Entity
@Table(name = "optimization_jobs")
public class OptimizationJob {
    
    @Id
    private String jobId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "route_id")
    private String routeId;
    
    @Lob
    @Column(name = "request_data")
    private String requestData;
    
    @Lob
    @Column(name = "result_data")
    private String resultData;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage;
    
    @Column(name = "estimated_completion_time")
    private LocalDateTime estimatedCompletionTime;

    // Constructors
    public OptimizationJob() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = JobStatus.PENDING;
        this.progressPercentage = 0;
    }
    
    public OptimizationJob(String jobId, String userId, String routeId) {
        this();
        this.jobId = jobId;
        this.userId = userId;
        this.routeId = routeId;
    }

    // Status enum
    public enum JobStatus {
        PENDING,        // Job created but not started
        PROCESSING,     // Job is being processed
        COMPLETED,      // Job completed successfully
        FAILED,         // Job failed with error
        CANCELLED       // Job was cancelled
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    
    public String getRequestData() { return requestData; }
    public void setRequestData(String requestData) { this.requestData = requestData; }
    
    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public LocalDateTime getEstimatedCompletionTime() { return estimatedCompletionTime; }
    public void setEstimatedCompletionTime(LocalDateTime estimatedCompletionTime) { 
        this.estimatedCompletionTime = estimatedCompletionTime; 
    }
}