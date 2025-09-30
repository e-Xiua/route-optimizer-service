package com.exiua.routeoptimizer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exiua.routeoptimizer.model.OptimizationJob;

@Repository
public interface OptimizationJobRepository extends JpaRepository<OptimizationJob, String> {
    
    /**
     * Find jobs by user ID
     */
    List<OptimizationJob> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find jobs by status
     */
    List<OptimizationJob> findByStatus(OptimizationJob.JobStatus status);
    
    /**
     * Find jobs that are currently processing
     */
    @Query("SELECT j FROM OptimizationJob j WHERE j.status IN ('PENDING', 'PROCESSING')")
    List<OptimizationJob> findActiveJobs();
    
    /**
     * Find jobs created after a specific date
     */
    List<OptimizationJob> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find jobs by route ID
     */
    List<OptimizationJob> findByRouteId(String routeId);
    
    /**
     * Find jobs that should be cleaned up (completed or failed jobs older than specified date)
     */
    @Query("SELECT j FROM OptimizationJob j WHERE j.status IN ('COMPLETED', 'FAILED', 'CANCELLED') AND j.completedAt < :cutoffDate")
    List<OptimizationJob> findJobsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count jobs by status
     */
    long countByStatus(OptimizationJob.JobStatus status);
    
    /**
     * Find the most recent job for a user
     */
    Optional<OptimizationJob> findFirstByUserIdOrderByCreatedAtDesc(String userId);
}