package com.exiua.routeoptimizer.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO;
import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO.EnrichedPOI;
import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO.Location;
import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO.RouteConstraints;
import com.exiua.routeoptimizer.dto.EnrichedRouteProcessingRequestDTO.RoutePreferences;
import com.exiua.routeoptimizer.service.ProcessingPOIBuilderService.EnrichedProcessingPOI;

/**
 * Servicio orquestador para construir requests de procesamiento de rutas
 * enriquecidos con datos de proveedores
 */
@Service
public class RouteProcessingRequestBuilderService {

    private static final Logger log = LoggerFactory.getLogger(RouteProcessingRequestBuilderService.class);
    
    @Autowired
    private ProcessingPOIBuilderService poiBuilderService;
    
    @Autowired
    private ProviderDataEnrichmentService providerDataEnrichmentService;

    /**
     * Construye un RouteProcessingRequest completo con datos enriquecidos de proveedores
     * 
     * @param userId ID del usuario/turista
     * @param providerIds Lista de IDs de proveedores a incluir en la ruta
     * @param touristPreferences Preferencias del turista (categorías preferidas)
     * @param optimizeFor Criterio de optimización (distance, time, cost, experience)
     * @param startLat Latitud de inicio
     * @param startLon Longitud de inicio
     * @param endLat Latitud de fin
     * @param endLon Longitud de fin
     * @return Request enriquecido listo para enviar al servicio de procesamiento
     */
    public EnrichedRouteProcessingRequestDTO buildEnrichedRequest(
            Long userId,
            List<Long> providerIds,
            List<String> touristPreferences,
            String optimizeFor,
            Double startLat,
            Double startLon,
            Double endLat,
            Double endLon) {
        
        log.info("Construyendo request enriquecido para usuario {} con {} proveedores", 
            userId, providerIds.size());
        
        EnrichedRouteProcessingRequestDTO request = new EnrichedRouteProcessingRequestDTO();
        
        // 1. Configuración básica
        request.setRouteId(generateRouteId(userId));
        request.setUserId(userId.toString());
        
        // 2. Construir POIs enriquecidos desde los proveedores
        List<EnrichedProcessingPOI> processingPOIs = 
            poiBuilderService.buildEnrichedPOIsBatch(providerIds);
        
        // Convertir a EnrichedPOI del DTO
        List<EnrichedPOI> enrichedPOIs = processingPOIs.stream()
            .map(this::convertToEnrichedPOI)
            .collect(Collectors.toList());
        
        request.setPois(enrichedPOIs);
        
        log.info("Se agregaron {} POIs enriquecidos al request", enrichedPOIs.size());
        
        // 3. Configurar preferencias
        RoutePreferences preferences = new RoutePreferences();
        preferences.setOptimizeFor(optimizeFor != null ? optimizeFor : "distance");
        preferences.setPreferredCategories(touristPreferences != null ? touristPreferences : new ArrayList<>());
        preferences.setAccessibilityRequired(false);
        
        // Calcular costo total estimado y establecer límite
        Double estimatedCost = enrichedPOIs.stream()
            .map(EnrichedPOI::getCost)
            .filter(cost -> cost != null && cost < 999999.0)
            .mapToDouble(Double::doubleValue)
            .sum();
        
        preferences.setMaxTotalCost(estimatedCost * 1.2); // 20% de margen
        
        request.setPreferences(preferences);
        
        // 4. Configurar restricciones
        RouteConstraints constraints = new RouteConstraints();
        
        if (startLat != null && startLon != null) {
            constraints.setStartLocation(new Location(startLat, startLon));
        }
        
        if (endLat != null && endLon != null) {
            constraints.setEndLocation(new Location(endLat, endLon));
        }
        
        // Establecer hora de inicio (ahora por defecto)
        constraints.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        constraints.setLunchBreakRequired(true);
        constraints.setLunchBreakDuration(60);
        
        request.setConstraints(constraints);
        
        log.info("Request enriquecido construido exitosamente: {} POIs, costo estimado: {}", 
            enrichedPOIs.size(), estimatedCost);
        
        return request;
    }

    /**
     * Construye un request simplificado con configuración por defecto
     * 
     * @param userId ID del usuario
     * @param providerIds Lista de IDs de proveedores
     * @return Request enriquecido con configuración por defecto
     */
    public EnrichedRouteProcessingRequestDTO buildSimpleRequest(Long userId, List<Long> providerIds) {
        return buildEnrichedRequest(
            userId,
            providerIds,
            new ArrayList<>(), // Sin preferencias específicas
            "distance", // Optimizar por distancia
            null, // Sin ubicación de inicio específica
            null,
            null, // Sin ubicación de fin específica
            null
        );
    }

    /**
     * Construye un request con filtrado de proveedores por categoría
     * 
     * @param userId ID del usuario
     * @param providerIds Lista de IDs de proveedores
     * @param requiredCategories Categorías requeridas para filtrar proveedores
     * @param optimizeFor Criterio de optimización
     * @return Request enriquecido con proveedores filtrados
     */
    public EnrichedRouteProcessingRequestDTO buildRequestWithCategoryFilter(
            Long userId,
            List<Long> providerIds,
            List<String> requiredCategories,
            String optimizeFor) {
        
        log.info("Construyendo request con filtro de categorías: {}", requiredCategories);
        
        // Construir POIs
        List<EnrichedProcessingPOI> processingPOIs = 
            poiBuilderService.buildEnrichedPOIsBatch(providerIds);
        
        // Filtrar POIs que tengan al menos una de las categorías requeridas
        List<EnrichedProcessingPOI> filteredPOIs = processingPOIs.stream()
            .filter(poi -> {
                if (requiredCategories == null || requiredCategories.isEmpty()) {
                    return true; // Sin filtro
                }
                
                List<String> poiCategories = poi.getCategories();
                if (poiCategories == null || poiCategories.isEmpty()) {
                    return false;
                }
                
                // Verificar si tiene al menos una categoría requerida
                return poiCategories.stream()
                    .anyMatch(requiredCategories::contains);
            })
            .collect(Collectors.toList());
        
        log.info("POIs filtrados: {} de {} cumplieron con las categorías requeridas", 
            filteredPOIs.size(), processingPOIs.size());
        
        // Construir request con POIs filtrados
        EnrichedRouteProcessingRequestDTO request = new EnrichedRouteProcessingRequestDTO();
        request.setRouteId(generateRouteId(userId));
        request.setUserId(userId.toString());
        
        List<EnrichedPOI> enrichedPOIs = filteredPOIs.stream()
            .map(this::convertToEnrichedPOI)
            .collect(Collectors.toList());
        
        request.setPois(enrichedPOIs);
        
        // Configurar preferencias
        RoutePreferences preferences = new RoutePreferences();
        preferences.setOptimizeFor(optimizeFor != null ? optimizeFor : "distance");
        preferences.setPreferredCategories(requiredCategories);
        request.setPreferences(preferences);
        
        // Configurar restricciones por defecto
        RouteConstraints constraints = new RouteConstraints();
        constraints.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        request.setConstraints(constraints);
        
        return request;
    }

    /**
     * Construye un request optimizado por costo
     * Solo incluye proveedores con costos válidos (que tienen servicios)
     * 
     * @param userId ID del usuario
     * @param providerIds Lista de IDs de proveedores
     * @param maxBudget Presupuesto máximo
     * @return Request enriquecido con proveedores que tienen costos válidos
     */
    public EnrichedRouteProcessingRequestDTO buildCostOptimizedRequest(
            Long userId,
            List<Long> providerIds,
            Double maxBudget) {
        
        log.info("Construyendo request optimizado por costo, presupuesto máximo: {}", maxBudget);
        
        // Filtrar proveedores con costos válidos
        List<Long> validProviders = poiBuilderService.filterProvidersWithValidCosts(providerIds);
        
        if (validProviders.isEmpty()) {
            log.warn("No se encontraron proveedores con costos válidos");
            return buildSimpleRequest(userId, providerIds); // Fallback
        }
        
        // Construir request
        EnrichedRouteProcessingRequestDTO request = buildEnrichedRequest(
            userId,
            validProviders,
            new ArrayList<>(),
            "cost", // Optimizar por costo
            null, null, null, null
        );
        
        // Configurar presupuesto máximo
        if (maxBudget != null) {
            request.getPreferences().setMaxTotalCost(maxBudget);
        }
        
        // Ordenar POIs por costo
        request.sortPOIsByCost();
        
        log.info("Request optimizado por costo construido con {} proveedores válidos", 
            validProviders.size());
        
        return request;
    }

    /**
     * Convierte un ProcessingPOI a EnrichedPOI del DTO
     */
    private EnrichedPOI convertToEnrichedPOI(EnrichedProcessingPOI processingPOI) {
        EnrichedPOI enrichedPOI = new EnrichedPOI();
        
        enrichedPOI.setId(processingPOI.getId());
        enrichedPOI.setName(processingPOI.getName());
        enrichedPOI.setLatitude(processingPOI.getLatitude());
        enrichedPOI.setLongitude(processingPOI.getLongitude());
        enrichedPOI.setCategory(processingPOI.getCategory());
        enrichedPOI.setSubcategory(processingPOI.getSubcategory());
        enrichedPOI.setVisitDuration(processingPOI.getVisitDuration());
        enrichedPOI.setCost(processingPOI.getCost());
        enrichedPOI.setRating(processingPOI.getRating());
        enrichedPOI.setOpeningHours(processingPOI.getOpeningHours());
        enrichedPOI.setDescription(processingPOI.getDescription());
        enrichedPOI.setImageUrl(processingPOI.getImageUrl());
        enrichedPOI.setAccessibility(processingPOI.getAccessibility());
        enrichedPOI.setProviderId(processingPOI.getProviderId());
        enrichedPOI.setProviderName(processingPOI.getProviderName());
        enrichedPOI.setCategories(processingPOI.getCategories());
        
        return enrichedPOI;
    }

    /**
     * Genera un ID único para la ruta
     */
    private String generateRouteId(Long userId) {
        return String.format("ROUTE_%d_%d", userId, System.currentTimeMillis());
    }
}
