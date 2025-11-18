package com.exiua.routeoptimizer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SystemStatsDTO {
    private int activeJobs;
    private int maxConcurrentJobs;
    private int totalJobsSubmitted;
    private int totalJobsCompleted;
    private int totalJobsFailed;
    private double successRate;

}
