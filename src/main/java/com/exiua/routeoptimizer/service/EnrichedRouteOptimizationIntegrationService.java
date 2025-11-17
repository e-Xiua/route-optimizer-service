package com.exiua.routeoptimizer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.service.ProcessingPOIBuilderService.EnrichedProcessingPOI;

/**
 * Servicio para integrar POIs enriquecidos con el sistema de optimización de rutas existente
 * Conecta el nuevo sistema de enriquecimiento con RouteOptimizationService y EnhancedRouteOptimizationService
 */
@Service
public class EnrichedRouteOptimizationIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(EnrichedRouteOptimizationIntegrationService.class);
    
    @Autowired
    private ProcessingPOIBuilderService poiBuilderService;

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
     * Convierte EnrichedProcessingPOI al modelo POI existente
     */
    private com.exiua.routeoptimizer.model.POI convertToPOI(EnrichedProcessingPOI enrichedPOI) {
        com.exiua.routeoptimizer.model.POI poi = new com.exiua.routeoptimizer.model.POI();

        log.info("Converting POI - Original ID: {}, Name: {}, ProviderID: {}", 
        enrichedPOI.getId(), enrichedPOI.getName(), enrichedPOI.getProviderId());
        
        // Basic identification
        poi.setId(enrichedPOI.getId());
        poi.setName(enrichedPOI.getName());
        poi.setLatitude(enrichedPOI.getLatitude());
        poi.setLongitude(enrichedPOI.getLongitude());
        
        // Categories (both array and single)
        List<String> categories = enrichedPOI.getCategories();
        if (categories != null && !categories.isEmpty()) {
            poi.setCategories(categories.toArray(new String[0]));
        } else {
            poi.setCategories(new String[0]);
        }
        poi.setCategory(enrichedPOI.getCategory()); // Single category string
        poi.setSubcategory(enrichedPOI.getSubcategory());
        
        // Timing and cost
        poi.setVisitDuration(enrichedPOI.getVisitDuration());
        poi.setCost(enrichedPOI.getCost());
        poi.setRating(enrichedPOI.getRating());
        
        // Additional information
        poi.setDescription(enrichedPOI.getDescription());
        poi.setOpeningHours(enrichedPOI.getOpeningHours());
        poi.setImageUrl(enrichedPOI.getImageUrl());
        poi.setAccessibility(enrichedPOI.getAccessibility());
        
        // Provider information
        poi.setProviderId(enrichedPOI.getProviderId());
        poi.setProviderName(enrichedPOI.getProviderName());
        
        log.debug("Convertido POI: {} (ProviderId: {}, Costo: {}, Categorías: {}, Description: {})", 
            enrichedPOI.getName(), enrichedPOI.getProviderId(), enrichedPOI.getCost(), 
            enrichedPOI.getCategories(), enrichedPOI.getDescription());
        
        return poi;
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
