package com.exiua.routeoptimizer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
import com.exiua.routeoptimizer.service.ProcessingPOIBuilderService.EnrichedProcessingPOI;

/**
 * Servicio para integrar POIs enriquecidos con el sistema de optimización de rutas existente
 * Conecta el nuevo sistema de enriquecimiento con RouteOptimizationService y EnhancedRouteOptimizationService
 */
@Service
public class EnrichedRouteOptimizationIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(EnrichedRouteOptimizationIntegrationService.class);
    
    @Autowired
    private RouteProcessingRequestBuilderService requestBuilderService;
    
    @Autowired
    private ProcessingPOIBuilderService poiBuilderService;
    
    @Autowired
    private OptimizationJobRepository jobRepository;

    /**
     * Crea un RouteOptimizationRequest desde POIs enriquecidos
     * Este método convierte los POIs enriquecidos al formato que espera el servicio de optimización existente
     * 
     * @param userId ID del usuario/turista
     * @param providerIds Lista de IDs de proveedores
     * @param optimizeFor Criterio de optimización
     * @param maxBudget Presupuesto máximo
     * @return RouteOptimizationRequest listo para enviar a EnhancedRouteOptimizationService
     */
    public RouteOptimizationRequest createOptimizationRequestFromProviders(
            Long userId,
            List<Long> providerIds,
            String optimizeFor,
            Double maxBudget) {
        
        log.info("Creando RouteOptimizationRequest para {} proveedores del usuario {}", 
            providerIds.size(), userId);
        
        // 1. Construir POIs enriquecidos
        List<EnrichedProcessingPOI> enrichedPOIs = poiBuilderService.buildEnrichedPOIsBatch(providerIds);
        
        if (enrichedPOIs.isEmpty()) {
            log.warn("No se pudieron construir POIs enriquecidos para los proveedores");
            throw new IllegalStateException("No se pudieron obtener datos de proveedores");
        }
        
        // 2. Crear RouteOptimizationRequest
        RouteOptimizationRequest request = new RouteOptimizationRequest();
        request.setUserId(userId.toString());
        request.setRouteId("ROUTE_" + userId + "_" + System.currentTimeMillis());
        
        // 3. Convertir EnrichedProcessingPOI a POI del modelo existente
        List<com.exiua.routeoptimizer.model.POI> pois = enrichedPOIs.stream()
            .map(this::convertToPOI)
            .collect(Collectors.toList());
        
        request.setPois(pois);
        
        // 4. Configurar criterio de optimización
        if (optimizeFor != null) {
            // El sistema existente usa RouteOptimizationRequest que podría necesitar
            // configuración adicional según el criterio
            log.info("Optimizando por: {}", optimizeFor);
        }
        
        log.info("RouteOptimizationRequest creado con {} POIs", pois.size());
        
        return request;
    }

    /**
     * Crea y envía un trabajo de optimización usando POIs enriquecidos
     * 
     * @param userId ID del usuario
     * @param providerIds Lista de IDs de proveedores
     * @param optimizeFor Criterio de optimización (distance, time, cost, experience)
     * @param maxBudget Presupuesto máximo (puede ser null)
     * @return ID del trabajo creado
     */
    public String submitOptimizationJobWithEnrichedPOIs(
            Long userId,
            List<Long> providerIds,
            String optimizeFor,
            Double maxBudget) {
        
        log.info("Iniciando trabajo de optimización con POIs enriquecidos");
        
        // Crear request de optimización
        RouteOptimizationRequest request = createOptimizationRequestFromProviders(
            userId, providerIds, optimizeFor, maxBudget);
        
        // Crear trabajo en la base de datos
        OptimizationJob job = new OptimizationJob();
        job.setJobId(request.getRouteId());
        job.setUserId(request.getUserId());
        job.setStatus(OptimizationJob.JobStatus.PENDING);
        job.setProgressPercentage(0);
        
        // Guardar información de POIs enriquecidos como metadata
        try {
            String enrichedMetadata = buildEnrichedMetadata(providerIds, optimizeFor, maxBudget);
            // Podrías agregar un campo metadata al OptimizationJob si lo necesitas
            log.debug("Metadata enriquecida: {}", enrichedMetadata);
        } catch (Exception e) {
            log.warn("Error construyendo metadata: {}", e.getMessage());
        }
        
        jobRepository.save(job);
        
        log.info("Trabajo de optimización creado con ID: {}", job.getJobId());
        
        return job.getJobId();
    }

    /**
     * Convierte EnrichedProcessingPOI al modelo POI existente
     */
    private com.exiua.routeoptimizer.model.POI convertToPOI(EnrichedProcessingPOI enrichedPOI) {
        com.exiua.routeoptimizer.model.POI poi = new com.exiua.routeoptimizer.model.POI();
        
        poi.setId(enrichedPOI.getId());
        poi.setName(enrichedPOI.getName());
        poi.setLatitude(enrichedPOI.getLatitude());
        poi.setLongitude(enrichedPOI.getLongitude());
        List<String> categories = enrichedPOI.getCategories();
        if (categories != null && !categories.isEmpty()) {
            poi.setCategories(categories.toArray(new String[0]));
        } else {
            poi.setCategories(new String[0]);
        }
        poi.setVisitDuration(enrichedPOI.getVisitDuration());
        
        // Agregar cost como atributo adicional si el modelo lo soporta
        // De lo contrario, el costo ya está incluido en la lógica de optimización
        
        log.debug("Convertido POI: {} (Costo: {}, Categorías: {})", 
            enrichedPOI.getName(), enrichedPOI.getCost(), enrichedPOI.getCategories());
        
        return poi;
    }

    /**
     * Construye metadata enriquecida para el trabajo
     */
    private String buildEnrichedMetadata(List<Long> providerIds, String optimizeFor, Double maxBudget) {
        StringBuilder metadata = new StringBuilder();
        metadata.append("Providers: ").append(providerIds.size());
        metadata.append(", OptimizeFor: ").append(optimizeFor != null ? optimizeFor : "distance");
        if (maxBudget != null) {
            metadata.append(", MaxBudget: ").append(maxBudget);
        }
        return metadata.toString();
    }

    /**
     * Obtiene el request enriquecido completo (útil para debugging o logs)
     */
    public EnrichedRouteProcessingRequestDTO getEnrichedRequest(
            Long userId,
            List<Long> providerIds,
            String optimizeFor,
            Double maxBudget) {
        
        if (maxBudget != null) {
            return requestBuilderService.buildCostOptimizedRequest(userId, providerIds, maxBudget);
        } else {
            return requestBuilderService.buildEnrichedRequest(
                userId, providerIds, null, optimizeFor, null, null, null, null);
        }
    }

    /**
     * Verifica si los proveedores tienen datos válidos para optimización
     */
    public boolean validateProvidersForOptimization(List<Long> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            log.warn("Lista de proveedores vacía");
            return false;
        }
        
        List<Long> validProviders = poiBuilderService.filterProvidersWithValidCosts(providerIds);
        
        if (validProviders.isEmpty()) {
            log.warn("Ningún proveedor tiene costos válidos");
            return false;
        }
        
        log.info("Validación exitosa: {}/{} proveedores tienen datos válidos", 
            validProviders.size(), providerIds.size());
        
        return true;
    }
}
