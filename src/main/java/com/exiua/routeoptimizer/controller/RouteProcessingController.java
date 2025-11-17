package com.exiua.routeoptimizer.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exiua.routeoptimizer.dto.EnrichedProviderData;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.service.EnhancedRouteOptimizationService;
import com.exiua.routeoptimizer.service.EnrichedRouteOptimizationIntegrationService;
import com.exiua.routeoptimizer.service.ProviderDataEnrichmentService;

/**
 * Controlador para construcción de requests de procesamiento de rutas enriquecidos
 */
@RestController
@RequestMapping("/api/route-processing")
public class RouteProcessingController {

    private static final Logger log = LoggerFactory.getLogger(RouteProcessingController.class);
        
    @Autowired
    private ProviderDataEnrichmentService providerDataEnrichmentService;
    
    @Autowired
    private EnrichedRouteOptimizationIntegrationService integrationService;
    
    @Autowired
    private EnhancedRouteOptimizationService enhancedOptimizationService;

    /**
     * DTO para recibir parámetros de construcción de ruta
     */
    public static class RouteRequestParams {
        private Long userId;
        private List<Long> providerIds;
        private List<String> touristPreferences;
        private String optimizeFor;
        private Double startLatitude;
        private Double startLongitude;
        private Double endLatitude;
        private Double endLongitude;
        private Double maxBudget;
        private List<String> requiredCategories;

        // Getters y Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public List<Long> getProviderIds() {
            return providerIds;
        }

        public void setProviderIds(List<Long> providerIds) {
            this.providerIds = providerIds;
        }

        public List<String> getTouristPreferences() {
            return touristPreferences;
        }

        public void setTouristPreferences(List<String> touristPreferences) {
            this.touristPreferences = touristPreferences;
        }

        public String getOptimizeFor() {
            return optimizeFor;
        }

        public void setOptimizeFor(String optimizeFor) {
            this.optimizeFor = optimizeFor;
        }

        public Double getStartLatitude() {
            return startLatitude;
        }

        public void setStartLatitude(Double startLatitude) {
            this.startLatitude = startLatitude;
        }

        public Double getStartLongitude() {
            return startLongitude;
        }

        public void setStartLongitude(Double startLongitude) {
            this.startLongitude = startLongitude;
        }

        public Double getEndLatitude() {
            return endLatitude;
        }

        public void setEndLatitude(Double endLatitude) {
            this.endLatitude = endLatitude;
        }

        public Double getEndLongitude() {
            return endLongitude;
        }

        public void setEndLongitude(Double endLongitude) {
            this.endLongitude = endLongitude;
        }

        public Double getMaxBudget() {
            return maxBudget;
        }

        public void setMaxBudget(Double maxBudget) {
            this.maxBudget = maxBudget;
        }

        public List<String> getRequiredCategories() {
            return requiredCategories;
        }

        public void setRequiredCategories(List<String> requiredCategories) {
            this.requiredCategories = requiredCategories;
        }
    }

    /**
     * Obtiene datos enriquecidos de un proveedor específico
     * 
     * GET /api/route-processing/provider/{id}/enriched
     */
    @GetMapping("/provider/{id}/enriched")
    public ResponseEntity<?> getEnrichedProviderData(@PathVariable Long id) {
        try {
            log.info("Solicitando datos enriquecidos para proveedor: {}", id);
            
            EnrichedProviderData enrichedData = providerDataEnrichmentService.getEnrichedProviderData(id);
            
            return ResponseEntity.ok(enrichedData);
            
        } catch (Exception e) {
            log.error("Error obteniendo datos enriquecidos del proveedor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error obteniendo datos del proveedor: " + e.getMessage());
        }
    }

    /**
     * Envía un trabajo de optimización de ruta con POIs enriquecidos
     * Este endpoint conecta el sistema de enriquecimiento con el servicio de optimización
     * 
     * POST /api/route-processing/submit-optimization-job
     */
    @PostMapping("/submit-optimization-job")
    public ResponseEntity<?> submitOptimizationJob(@RequestBody RouteRequestParams params) {
        try {
            log.info("Recibiendo solicitud de optimización de ruta para usuario {}", params.getUserId());
            
            if (params.getUserId() == null || params.getProviderIds() == null || params.getProviderIds().isEmpty()) {
                return ResponseEntity.badRequest().body("userId y providerIds son requeridos");
            }
            
            // Validar que los proveedores tengan datos válidos
            if (!integrationService.validateProvidersForOptimization(params.getProviderIds())) {
                return ResponseEntity.badRequest()
                    .body("Los proveedores no tienen datos válidos para optimización");
            }
            
            // Crear y enviar trabajo de optimización usando POIs enriquecidos
            RouteOptimizationRequest request = integrationService.createOptimizationRequestFromProviders(
                params.getUserId(),
                params.getProviderIds(),
                params.getOptimizeFor(),
                params.getMaxBudget()
            );
            
            // Enviar al servicio de optimización mejorado
            JobSubmissionResponseDTO jobResponse = enhancedOptimizationService.submitOptimizationRequest(request);
            
            log.info("Trabajo de optimización enviado exitosamente: {}", jobResponse.getJobId());
            
            return ResponseEntity.accepted().body(jobResponse);
            
        } catch (Exception e) {
            log.error("Error enviando trabajo de optimización: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error enviando trabajo: " + e.getMessage());
        }
    }

}
