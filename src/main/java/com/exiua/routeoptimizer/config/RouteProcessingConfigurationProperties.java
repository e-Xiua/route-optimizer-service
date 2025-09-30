package com.exiua.routeoptimizer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Route Processing Service Configuration Properties
 */
@Configuration
@ConfigurationProperties(prefix = "route.processing.service")
public class RouteProcessingConfigurationProperties {
    
    private String url;
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
}