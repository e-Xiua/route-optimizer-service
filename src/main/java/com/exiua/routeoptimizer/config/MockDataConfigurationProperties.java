package com.exiua.routeoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for mock data
 */
@Configuration
@ConfigurationProperties(prefix = "mock.data")
public class MockDataConfigurationProperties {
    
    private boolean enabled = true;
    private int defaultPoiCount = 8;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultPoiCount() {
        return defaultPoiCount;
    }

    public void setDefaultPoiCount(int defaultPoiCount) {
        this.defaultPoiCount = defaultPoiCount;
    }
}