package com.exiua.routeoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for optimization settings
 */
@Configuration
@ConfigurationProperties(prefix = "optimization")
public class OptimizationConfigurationProperties {
    
    private boolean asyncEnabled = true;
    private int simulationDelaySeconds = 5;
    private int maxConcurrentJobs = 10;

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public void setAsyncEnabled(boolean asyncEnabled) {
        this.asyncEnabled = asyncEnabled;
    }

    public int getSimulationDelaySeconds() {
        return simulationDelaySeconds;
    }

    public void setSimulationDelaySeconds(int simulationDelaySeconds) {
        this.simulationDelaySeconds = simulationDelaySeconds;
    }

    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }

    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }
}