package com.exiua.routeoptimizer.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.dto.ProveedorDTO;

/**
 * Servicio para construir objetos ProcessingPOI enriquecidos con datos de proveedores
 * Este servicio crea POIs listos para ser enviados al route-processing-service
 */
@Service
public class ProcessingPOIBuilderService {

    private static final Logger log = LoggerFactory.getLogger(ProcessingPOIBuilderService.class);
    
    @Autowired
    private ProviderDataEnrichmentService providerDataEnrichmentService;

    /**
     * Clase interna que replica ProcessingPOI para evitar dependencias circulares
     * En producción, deberías tener esta clase en un módulo común compartido
     */
    public static class EnrichedProcessingPOI {
        private Long id;
        private String name;
        private Double latitude;
        private Double longitude;
        private String category;
        private String subcategory;
        private Integer visitDuration;
        private Double cost;
        private Double rating;
        private String openingHours;
        private String description;
        private String imageUrl;
        private Boolean accessibility;
        private Long providerId;
        private String providerName;
        private List<String> categories; // Preferencias del proveedor

        // Getters y Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSubcategory() {
            return subcategory;
        }

        public void setSubcategory(String subcategory) {
            this.subcategory = subcategory;
        }

        public Integer getVisitDuration() {
            return visitDuration;
        }

        public void setVisitDuration(Integer visitDuration) {
            this.visitDuration = visitDuration;
        }

        public Double getCost() {
            return cost;
        }

        public void setCost(Double cost) {
            this.cost = cost;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public String getOpeningHours() {
            return openingHours;
        }

        public void setOpeningHours(String openingHours) {
            this.openingHours = openingHours;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Boolean getAccessibility() {
            return accessibility;
        }

        public void setAccessibility(Boolean accessibility) {
            this.accessibility = accessibility;
        }

        public Long getProviderId() {
            return providerId;
        }

        public void setProviderId(Long providerId) {
            this.providerId = providerId;
        }

        public String getProviderName() {
            return providerName;
        }

        public void setProviderName(String providerName) {
            this.providerName = providerName;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }
    }

    /**
     * Construye múltiples POIs enriquecidos en batch
     * 
     * @param providerIds Lista de IDs de proveedores
     * @return Lista de POIs enriquecidos
     */
    public List<EnrichedProcessingPOI> buildEnrichedPOIsBatch(List<Long> providerIds) {

        log.info("Building POIs for provider IDs: {}", providerIds);
        log.info("Construyendo {} POIs enriquecidos en batch", providerIds.size());
        
        // Obtener datos enriquecidos de todos los proveedores
        Map<Long, ProviderDataEnrichmentService.EnrichedProviderData> enrichedDataMap = 
            providerDataEnrichmentService.getEnrichedProviderDataBatch(providerIds);
        
        // Construir POIs
        List<EnrichedProcessingPOI> pois = providerIds.stream()
            .map(providerId -> {
                try {
                    ProviderDataEnrichmentService.EnrichedProviderData enrichedData = 
                        enrichedDataMap.get(providerId);
                    
                    if (enrichedData == null) {
                        log.warn("No hay datos enriquecidos para proveedor {}", providerId);
                        return null;
                    }
                    
                    return buildPOIFromEnrichedData(providerId, enrichedData);
                    
                } catch (Exception e) {
                    log.error("Error construyendo POI para proveedor {}: {}", 
                        providerId, e.getMessage());
                    return null;
                }
            })
            .filter(poi -> poi != null)
            .collect(Collectors.toList());
        
        log.info("Se construyeron {} POIs exitosamente de {} solicitados", 
            pois.size(), providerIds.size());
        
        return pois;
    }

    /**
     * Construye un POI desde datos enriquecidos ya obtenidos
     * 
     * @param providerId ID del proveedor
     * @param enrichedData Datos enriquecidos del proveedor
     * @return POI enriquecido
     */
    private EnrichedProcessingPOI buildPOIFromEnrichedData(
            Long providerId, 
            ProviderDataEnrichmentService.EnrichedProviderData enrichedData) {
        
        EnrichedProcessingPOI poi = new EnrichedProcessingPOI();
        ProveedorDTO provider = enrichedData.getProvider();
        
        
        poi.setId(providerId);
        poi.setProviderId(providerId);
    
        // Log any ID mismatches for debugging
        if (provider.getIdProveedor() != null && !providerId.equals(provider.getIdProveedor())) {
        log.warn("Provider ID mismatch detected! Requested: {}, Database has: {}, Name: {}", 
            providerId, provider.getIdProveedor(), provider.getNombre_empresa());
            }
        
        // Manejar nombre de empresa null
        String empresaName = provider.getNombre_empresa() != null ? provider.getNombre_empresa() : "Proveedor " + provider.getIdProveedor();
        poi.setName(empresaName);
        
        // Manejar nombre de proveedor null
        String providerName = provider.getNombre() != null ? provider.getNombre() : "Proveedor " + provider.getIdProveedor();
        poi.setProviderName(providerName);
        
        // Coordenadas
        try {
            String coordX = provider.getCoordenadaX();
            String coordY = provider.getCoordenadaY();
            
            if (coordX != null && coordY != null && !coordX.trim().isEmpty() && !coordY.trim().isEmpty()) {
                poi.setLatitude(Double.parseDouble(coordX.trim()));
                poi.setLongitude(Double.parseDouble(coordY.trim()));
            } else {
                log.warn("Coordenadas vacías o null para proveedor {}", providerId);
                poi.setLatitude(0.0);
                poi.setLongitude(0.0);
            }
        } catch (NumberFormatException e) {
            log.warn("Error parseando coordenadas para proveedor {}: {}", providerId, e.getMessage());
            poi.setLatitude(0.0);
            poi.setLongitude(0.0);
        }
        
        // Costo promedio
        poi.setCost(enrichedData.getAverageCost());
        
        // Categorías
        poi.setCategories(enrichedData.getCategories());
        
        // Categoría principal
        if (!enrichedData.getCategories().isEmpty()) {
            poi.setCategory(enrichedData.getCategories().get(0));
        } else {
            poi.setCategory("Sin_Categoria");
        }
        
        // Información adicional
        poi.setDescription("Proveedor: " + empresaName);
        poi.setOpeningHours("Consultar con proveedor");
        poi.setAccessibility(false);
        poi.setRating(0.0);
        // Usar tiempo promedio de los servicios del proveedor
        poi.setVisitDuration(enrichedData.getAverageVisitDuration());
        
        return poi;
    }

    /**
     * Verifica si un proveedor tiene costos válidos (no es HIGH_COST_VALUE)
     * 
     * @param providerId ID del proveedor
     * @return true si tiene costos válidos, false si no tiene servicios con precio
     */
    public boolean hasValidCosts(Long providerId) {
        Double avgCost = providerDataEnrichmentService.getProviderAverageCost(providerId);
        return avgCost < 999999.0; // Menor que HIGH_COST_VALUE
    }

    /**
     * Filtra proveedores que tienen costos válidos
     * 
     * @param providerIds Lista de IDs de proveedores
     * @return Lista de IDs de proveedores con costos válidos
     */
    public List<Long> filterProvidersWithValidCosts(List<Long> providerIds) {
        log.info("Filtrando {} proveedores con costos válidos", providerIds.size());
        
        List<Long> validProviders = providerIds.stream()
            .filter(this::hasValidCosts)
            .collect(Collectors.toList());
        
        log.info("Se encontraron {} proveedores con costos válidos de {} total", 
            validProviders.size(), providerIds.size());
        
        return validProviders;
    }
}
