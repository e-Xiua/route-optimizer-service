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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.service.EnhancedRouteOptimizationService;
import com.exiua.routeoptimizer.service.EnrichedRouteOptimizationIntegrationService;
import com.exiua.routeoptimizer.service.ProcessingPOIBuilderService;
import com.exiua.routeoptimizer.service.ProviderDataEnrichmentService;
import com.exiua.routeoptimizer.service.ProviderDataEnrichmentService.EnrichedProviderData;
import com.exiua.routeoptimizer.service.RouteProcessingRequestBuilderService;

/**
 * Controlador para construcción de requests de procesamiento de rutas enriquecidos
 */
@RestController
@RequestMapping("/api/route-processing")
public class RouteProcessingController {

    private static final Logger log = LoggerFactory.getLogger(RouteProcessingController.class);
    
    @Autowired
    private RouteProcessingRequestBuilderService requestBuilderService;
    
    @Autowired
    private ProviderDataEnrichmentService providerDataEnrichmentService;
    
    @Autowired
    private ProcessingPOIBuilderService poiBuilderService;
    
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
     * Obtiene el costo promedio de un proveedor
     * 
     * GET /api/route-processing/provider/{id}/average-cost
     */
    @GetMapping("/provider/{id}/average-cost")
    public ResponseEntity<?> getProviderAverageCost(@PathVariable Long id) {
        try {
            log.info("Solicitando costo promedio para proveedor: {}", id);
            
            Double avgCost = providerDataEnrichmentService.getProviderAverageCost(id);
            
            return ResponseEntity.ok(new AverageCostResponse(id, avgCost, avgCost < 999999.0));
            
        } catch (Exception e) {
            log.error("Error obteniendo costo promedio del proveedor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error obteniendo costo: " + e.getMessage());
        }
    }

    /**
     * Obtiene las categorías de un proveedor
     * 
     * GET /api/route-processing/provider/{id}/categories
     */
    @GetMapping("/provider/{id}/categories")
    public ResponseEntity<?> getProviderCategories(@PathVariable Long id) {
        try {
            log.info("Solicitando categorías para proveedor: {}", id);
            
            List<String> categories = providerDataEnrichmentService.getProviderCategories(id);
            
            return ResponseEntity.ok(categories);
            
        } catch (Exception e) {
            log.error("Error obteniendo categorías del proveedor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error obteniendo categorías: " + e.getMessage());
        }
    }

    /**
     * Construye un request de procesamiento de ruta completo y enriquecido
     * 
     * POST /api/route-processing/build-request
     */
    @PostMapping("/build-request")
    public ResponseEntity<?> buildEnrichedRequest(@RequestBody RouteRequestParams params) {
        try {
            log.info("Construyendo request enriquecido para usuario {} con {} proveedores", 
                params.getUserId(), params.getProviderIds() != null ? params.getProviderIds().size() : 0);
            
            if (params.getUserId() == null) {
                return ResponseEntity.badRequest().body("userId es requerido");
            }
            
            if (params.getProviderIds() == null || params.getProviderIds().isEmpty()) {
                return ResponseEntity.badRequest().body("providerIds no puede estar vacío");
            }
            
            EnrichedRouteProcessingRequestDTO request = requestBuilderService.buildEnrichedRequest(
                params.getUserId(),
                params.getProviderIds(),
                params.getTouristPreferences(),
                params.getOptimizeFor(),
                params.getStartLatitude(),
                params.getStartLongitude(),
                params.getEndLatitude(),
                params.getEndLongitude()
            );
            
            log.info("Request enriquecido construido exitosamente: {} POIs", 
                request.getPois() != null ? request.getPois().size() : 0);
            
            return ResponseEntity.ok(request);
            
        } catch (Exception e) {
            log.error("Error construyendo request enriquecido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error construyendo request: " + e.getMessage());
        }
    }

    /**
     * Construye un request simple con configuración por defecto
     * 
     * POST /api/route-processing/build-simple-request
     */
    @PostMapping("/build-simple-request")
    public ResponseEntity<?> buildSimpleRequest(
            @RequestParam Long userId,
            @RequestBody List<Long> providerIds) {
        
        try {
            log.info("Construyendo request simple para usuario {} con {} proveedores", 
                userId, providerIds.size());
            
            EnrichedRouteProcessingRequestDTO request = 
                requestBuilderService.buildSimpleRequest(userId, providerIds);
            
            return ResponseEntity.ok(request);
            
        } catch (Exception e) {
            log.error("Error construyendo request simple: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error construyendo request: " + e.getMessage());
        }
    }

    /**
     * Construye un request con filtrado por categorías
     * 
     * POST /api/route-processing/build-category-filtered-request
     */
    @PostMapping("/build-category-filtered-request")
    public ResponseEntity<?> buildCategoryFilteredRequest(@RequestBody RouteRequestParams params) {
        try {
            log.info("Construyendo request filtrado por categorías para usuario {}", params.getUserId());
            
            if (params.getUserId() == null || params.getProviderIds() == null || 
                params.getRequiredCategories() == null) {
                return ResponseEntity.badRequest()
                    .body("userId, providerIds y requiredCategories son requeridos");
            }
            
            EnrichedRouteProcessingRequestDTO request = 
                requestBuilderService.buildRequestWithCategoryFilter(
                    params.getUserId(),
                    params.getProviderIds(),
                    params.getRequiredCategories(),
                    params.getOptimizeFor()
                );
            
            return ResponseEntity.ok(request);
            
        } catch (Exception e) {
            log.error("Error construyendo request filtrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error construyendo request: " + e.getMessage());
        }
    }

    /**
     * Construye un request optimizado por costo
     * 
     * POST /api/route-processing/build-cost-optimized-request
     */
    @PostMapping("/build-cost-optimized-request")
    public ResponseEntity<?> buildCostOptimizedRequest(@RequestBody RouteRequestParams params) {
        try {
            log.info("Construyendo request optimizado por costo para usuario {}", params.getUserId());
            
            if (params.getUserId() == null || params.getProviderIds() == null) {
                return ResponseEntity.badRequest().body("userId y providerIds son requeridos");
            }
            
            EnrichedRouteProcessingRequestDTO request = 
                requestBuilderService.buildCostOptimizedRequest(
                    params.getUserId(),
                    params.getProviderIds(),
                    params.getMaxBudget()
                );
            
            return ResponseEntity.ok(request);
            
        } catch (Exception e) {
            log.error("Error construyendo request optimizado por costo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error construyendo request: " + e.getMessage());
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

    /**
     * Valida si una lista de proveedores es adecuada para optimización
     * 
     * POST /api/route-processing/validate-providers
     */
    @PostMapping("/validate-providers")
    public ResponseEntity<?> validateProviders(@RequestBody List<Long> providerIds) {
        try {
            log.info("Validando {} proveedores para optimización", providerIds.size());
            
            boolean isValid = integrationService.validateProvidersForOptimization(providerIds);
            
            if (isValid) {
                List<Long> validProviders = poiBuilderService.filterProvidersWithValidCosts(providerIds);
                return ResponseEntity.ok(new ValidationResponse(
                    true,
                    "Proveedores válidos para optimización",
                    validProviders.size(),
                    providerIds.size()
                ));
            } else {
                return ResponseEntity.ok(new ValidationResponse(
                    false,
                    "Proveedores no tienen datos suficientes",
                    0,
                    providerIds.size()
                ));
            }
            
        } catch (Exception e) {
            log.error("Error validando proveedores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error en validación: " + e.getMessage());
        }
    }

    /**
     * Response class para costo promedio
     */
    public static class AverageCostResponse {
        private Long providerId;
        private Double averageCost;
        private Boolean hasValidCost;

        public AverageCostResponse(Long providerId, Double averageCost, Boolean hasValidCost) {
            this.providerId = providerId;
            this.averageCost = averageCost;
            this.hasValidCost = hasValidCost;
        }

        public Long getProviderId() {
            return providerId;
        }

        public void setProviderId(Long providerId) {
            this.providerId = providerId;
        }

        public Double getAverageCost() {
            return averageCost;
        }

        public void setAverageCost(Double averageCost) {
            this.averageCost = averageCost;
        }

        public Boolean getHasValidCost() {
            return hasValidCost;
        }

        public void setHasValidCost(Boolean hasValidCost) {
            this.hasValidCost = hasValidCost;
        }
    }

    /**
     * Response class para validación de proveedores
     */
    public static class ValidationResponse {
        private Boolean valid;
        private String message;
        private Integer validProvidersCount;
        private Integer totalProvidersCount;

        public ValidationResponse(Boolean valid, String message, Integer validCount, Integer totalCount) {
            this.valid = valid;
            this.message = message;
            this.validProvidersCount = validCount;
            this.totalProvidersCount = totalCount;
        }

        public Boolean getValid() {
            return valid;
        }

        public void setValid(Boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getValidProvidersCount() {
            return validProvidersCount;
        }

        public void setValidProvidersCount(Integer validProvidersCount) {
            this.validProvidersCount = validProvidersCount;
        }

        public Integer getTotalProvidersCount() {
            return totalProvidersCount;
        }

        public void setTotalProvidersCount(Integer totalProvidersCount) {
            this.totalProvidersCount = totalProvidersCount;
        }
    }
}
