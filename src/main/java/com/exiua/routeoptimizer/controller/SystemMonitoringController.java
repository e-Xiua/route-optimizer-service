package com.exiua.routeoptimizer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exiua.routeoptimizer.service.EnhancedRouteOptimizationService;
import com.exiua.routeoptimizer.service.EnhancedRouteOptimizationService.SystemStatsDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para monitoreo y estadísticas del sistema
 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System Monitoring", description = "API para monitoreo del sistema de optimización")
public class SystemMonitoringController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemMonitoringController.class);
    
    private final EnhancedRouteOptimizationService optimizationService;
    
    public SystemMonitoringController(EnhancedRouteOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }
    
    /**
     * Obtener estadísticas del sistema
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas del sistema", 
               description = "Devuelve estadísticas sobre trabajos activos, completados y fallidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        try {
            SystemStatsDTO stats = optimizationService.getSystemStats();
            
            SystemStatsResponse response = new SystemStatsResponse();
            response.setActiveJobs(stats.getActiveJobs());
            response.setMaxConcurrentJobs(stats.getMaxConcurrentJobs());
            response.setTotalJobsSubmitted(stats.getTotalJobsSubmitted());
            response.setTotalJobsCompleted(stats.getTotalJobsCompleted());
            response.setTotalJobsFailed(stats.getTotalJobsFailed());
            response.setSuccessRate(stats.getSuccessRate());
            response.setSystemLoad(calculateSystemLoad(stats));
            response.setStatus(determineSystemStatus(stats));
            response.setTimestamp(java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas del sistema", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Verificar si el sistema puede aceptar más trabajos
     */
    @GetMapping("/capacity")
    @Operation(summary = "Verificar capacidad del sistema", 
               description = "Indica si el sistema puede aceptar nuevos trabajos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información de capacidad obtenida"),
        @ApiResponse(responseCode = "503", description = "Sistema ocupado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<CapacityResponse> getSystemCapacity() {
        try {
            SystemStatsDTO stats = optimizationService.getSystemStats();
            
            CapacityResponse response = new CapacityResponse();
            response.setActiveJobs(stats.getActiveJobs());
            response.setMaxConcurrentJobs(stats.getMaxConcurrentJobs());
            response.setAvailableCapacity(stats.getMaxConcurrentJobs() - stats.getActiveJobs());
            response.setCanAcceptJobs(stats.getActiveJobs() < stats.getMaxConcurrentJobs());
            response.setCapacityUtilization((double) stats.getActiveJobs() / stats.getMaxConcurrentJobs() * 100);
            response.setTimestamp(java.time.LocalDateTime.now().toString());
            
            if (response.isCanAcceptJobs()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error verificando capacidad del sistema", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Health check extendido con información del sistema
     */
    @GetMapping("/health")
    @Operation(summary = "Health check extendido", 
               description = "Verificación de salud del sistema con métricas detalladas")
    public ResponseEntity<HealthResponse> extendedHealthCheck() {
        try {
            SystemStatsDTO stats = optimizationService.getSystemStats();
            
            HealthResponse response = new HealthResponse();
            response.setStatus("UP");
            response.setService("Route Optimizer Service Enhanced");
            response.setVersion("2.0.0");
            response.setTimestamp(java.time.LocalDateTime.now().toString());
            
            // Componentes del sistema
            response.getComponents().put("optimization_service", stats.getActiveJobs() < stats.getMaxConcurrentJobs() ? "UP" : "BUSY");
            response.getComponents().put("database", "UP"); // Simplificado - en producción verificar conexión DB
            response.getComponents().put("thread_pool", stats.getActiveJobs() < stats.getMaxConcurrentJobs() ? "HEALTHY" : "STRESSED");
            
            // Métricas
            response.getMetrics().put("active_jobs", stats.getActiveJobs());
            response.getMetrics().put("total_jobs_submitted", stats.getTotalJobsSubmitted());
            response.getMetrics().put("total_jobs_completed", stats.getTotalJobsCompleted());
            response.getMetrics().put("success_rate", stats.getSuccessRate());
            response.getMetrics().put("system_load", calculateSystemLoad(stats));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error en health check extendido", e);
            
            HealthResponse errorResponse = new HealthResponse();
            errorResponse.setStatus("DOWN");
            errorResponse.setService("Route Optimizer Service Enhanced");
            errorResponse.setVersion("2.0.0");
            errorResponse.setTimestamp(java.time.LocalDateTime.now().toString());
            errorResponse.setError(e.getMessage());
            
            return ResponseEntity.status(503).body(errorResponse);
        }
    }
    
    // Métodos auxiliares
    
    private double calculateSystemLoad(SystemStatsDTO stats) {
        if (stats.getMaxConcurrentJobs() == 0) return 0.0;
        return (double) stats.getActiveJobs() / stats.getMaxConcurrentJobs() * 100;
    }
    
    private String determineSystemStatus(SystemStatsDTO stats) {
        double load = calculateSystemLoad(stats);
        if (load < 50) return "LIGHT";
        else if (load < 80) return "MODERATE";
        else if (load < 100) return "HEAVY";
        else return "OVERLOADED";
    }
    
    // DTOs de respuesta
    
    public static class SystemStatsResponse {
        private int activeJobs;
        private int maxConcurrentJobs;
        private int totalJobsSubmitted;
        private int totalJobsCompleted;
        private int totalJobsFailed;
        private double successRate;
        private double systemLoad;
        private String status;
        private String timestamp;
        
        // Getters y setters
        public int getActiveJobs() { return activeJobs; }
        public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
        
        public int getMaxConcurrentJobs() { return maxConcurrentJobs; }
        public void setMaxConcurrentJobs(int maxConcurrentJobs) { this.maxConcurrentJobs = maxConcurrentJobs; }
        
        public int getTotalJobsSubmitted() { return totalJobsSubmitted; }
        public void setTotalJobsSubmitted(int totalJobsSubmitted) { this.totalJobsSubmitted = totalJobsSubmitted; }
        
        public int getTotalJobsCompleted() { return totalJobsCompleted; }
        public void setTotalJobsCompleted(int totalJobsCompleted) { this.totalJobsCompleted = totalJobsCompleted; }
        
        public int getTotalJobsFailed() { return totalJobsFailed; }
        public void setTotalJobsFailed(int totalJobsFailed) { this.totalJobsFailed = totalJobsFailed; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getSystemLoad() { return systemLoad; }
        public void setSystemLoad(double systemLoad) { this.systemLoad = systemLoad; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    public static class CapacityResponse {
        private int activeJobs;
        private int maxConcurrentJobs;
        private int availableCapacity;
        private boolean canAcceptJobs;
        private double capacityUtilization;
        private String timestamp;
        
        // Getters y setters
        public int getActiveJobs() { return activeJobs; }
        public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
        
        public int getMaxConcurrentJobs() { return maxConcurrentJobs; }
        public void setMaxConcurrentJobs(int maxConcurrentJobs) { this.maxConcurrentJobs = maxConcurrentJobs; }
        
        public int getAvailableCapacity() { return availableCapacity; }
        public void setAvailableCapacity(int availableCapacity) { this.availableCapacity = availableCapacity; }
        
        public boolean isCanAcceptJobs() { return canAcceptJobs; }
        public void setCanAcceptJobs(boolean canAcceptJobs) { this.canAcceptJobs = canAcceptJobs; }
        
        public double getCapacityUtilization() { return capacityUtilization; }
        public void setCapacityUtilization(double capacityUtilization) { this.capacityUtilization = capacityUtilization; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    public static class HealthResponse {
        private String status;
        private String service;
        private String version;
        private String timestamp;
        private String error;
        private java.util.Map<String, String> components = new java.util.HashMap<>();
        private java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        
        // Getters y setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public java.util.Map<String, String> getComponents() { return components; }
        public void setComponents(java.util.Map<String, String> components) { this.components = components; }
        
        public java.util.Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(java.util.Map<String, Object> metrics) { this.metrics = metrics; }
    }
}