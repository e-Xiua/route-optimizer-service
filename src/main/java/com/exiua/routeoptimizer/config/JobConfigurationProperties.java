package com.exiua.routeoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for job management
 */
@Configuration
@ConfigurationProperties(prefix = "job")
public class JobConfigurationProperties {
    
    private int defaultTimeoutMinutes = 10;
    private int pollingIntervalSeconds = 2;
    private int cleanupIntervalHours = 24;

    public int getDefaultTimeoutMinutes() {
        return defaultTimeoutMinutes;
    }

    public void setDefaultTimeoutMinutes(int defaultTimeoutMinutes) {
        this.defaultTimeoutMinutes = defaultTimeoutMinutes;
    }

    public int getPollingIntervalSeconds() {
        return pollingIntervalSeconds;
    }

    public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
        this.pollingIntervalSeconds = pollingIntervalSeconds;
    }

    public int getCleanupIntervalHours() {
        return cleanupIntervalHours;
    }

    public void setCleanupIntervalHours(int cleanupIntervalHours) {
        this.cleanupIntervalHours = cleanupIntervalHours;
    }
}