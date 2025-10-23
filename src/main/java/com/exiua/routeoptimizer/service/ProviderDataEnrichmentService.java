package com.exiua.routeoptimizer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.client.PreferenciasApiClient;
import com.exiua.routeoptimizer.client.ProviderApiClient;
import com.exiua.routeoptimizer.client.ServicioApiClient;
import com.exiua.routeoptimizer.dto.ProveedorDTO;
import com.exiua.routeoptimizer.dto.ServicioDTO;
import com.exiua.routeoptimizer.dto.ServicioXPreferenciaDTO;

/**
 * Servicio para enriquecer datos de proveedores con información de servicios,
 * costos promedio y categorías (preferencias)
 */
@Service
public class ProviderDataEnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(ProviderDataEnrichmentService.class);
    
    // Costo muy alto para proveedores sin servicios o sin precio
    private static final Double HIGH_COST_VALUE = 999999.99;
    
    @Autowired
    private ProviderApiClient providerApiClient;
    
    @Autowired
    private ServicioApiClient servicioApiClient;
    
    @Autowired
    private PreferenciasApiClient preferenciasApiClient;

    /**
     * Datos enriquecidos del proveedor
     */
    public static class EnrichedProviderData {
        private ProveedorDTO provider;
        private List<ServicioDTO> services;
        private Double averageCost;
        private List<String> categories; // Nombres de preferencias
        private Map<String, Object> metadata;

        public EnrichedProviderData() {
            this.services = new ArrayList<>();
            this.categories = new ArrayList<>();
            this.metadata = new HashMap<>();
        }

        // Getters y Setters
        public ProveedorDTO getProvider() {
            return provider;
        }

        public void setProvider(ProveedorDTO provider) {
            this.provider = provider;
        }

        public List<ServicioDTO> getServices() {
            return services;
        }

        public void setServices(List<ServicioDTO> services) {
            this.services = services;
        }

        public Double getAverageCost() {
            return averageCost;
        }

        public void setAverageCost(Double averageCost) {
            this.averageCost = averageCost;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Obtiene datos enriquecidos de un proveedor
     * 
     * @param providerId ID del proveedor
     * @return Datos enriquecidos del proveedor
     */
    public EnrichedProviderData getEnrichedProviderData(Long providerId) {
        log.info("Obteniendo datos enriquecidos para proveedor ID: {}", providerId);
        
        EnrichedProviderData enrichedData = new EnrichedProviderData();
        
        try {
            // 1. Obtener datos básicos del proveedor
            ProveedorDTO provider = providerApiClient.obtenerProveedor(providerId);
            enrichedData.setProvider(provider);
            log.debug("Proveedor obtenido: {}", provider.getNombre_empresa());
            
            // 2. Obtener servicios del proveedor
            List<ServicioDTO> services = servicioApiClient.obtenerServiciosPorProveedor(providerId);
            enrichedData.setServices(services);
            log.debug("Servicios encontrados: {}", services.size());
            
            // 3. Calcular costo promedio
            Double averageCost = calculateAverageCost(services);
            enrichedData.setAverageCost(averageCost);
            log.debug("Costo promedio calculado: {}", averageCost);
            
            // 4. Obtener categorías (preferencias) de todos los servicios
            List<String> categories = extractCategoriesFromServices(services);
            enrichedData.setCategories(categories);
            log.debug("Categorías encontradas: {}", categories);
            
            // 5. Metadata adicional
            enrichedData.getMetadata().put("totalServices", services.size());
            enrichedData.getMetadata().put("activeServices", 
                services.stream().filter(ServicioDTO::isEstado).count());
            enrichedData.getMetadata().put("hasValidCost", averageCost < HIGH_COST_VALUE);
            
        } catch (Exception e) {
            log.error("Error obteniendo datos enriquecidos para proveedor {}: {}", 
                providerId, e.getMessage(), e);
            
            // Establecer valores por defecto en caso de error
            enrichedData.setAverageCost(HIGH_COST_VALUE);
            enrichedData.setServices(new ArrayList<>());
            enrichedData.setCategories(new ArrayList<>());
        }
        
        return enrichedData;
    }

    /**
     * Calcula el costo promedio de los servicios del proveedor
     * Si no tiene servicios o todos tienen precio null/0, retorna un valor muy alto
     * 
     * @param services Lista de servicios
     * @return Costo promedio o HIGH_COST_VALUE si no hay precios válidos
     */
    private Double calculateAverageCost(List<ServicioDTO> services) {
        if (services == null || services.isEmpty()) {
            log.warn("No hay servicios disponibles, retornando costo alto");
            return HIGH_COST_VALUE;
        }
        
        // Filtrar servicios activos con precio válido
        List<Double> validPrices = services.stream()
            .filter(ServicioDTO::isEstado) // Solo servicios activos
            .map(ServicioDTO::getPrecio)
            .filter(precio -> precio != null && precio > 0)
            .collect(Collectors.toList());
        
        if (validPrices.isEmpty()) {
            log.warn("No hay precios válidos, retornando costo alto");
            return HIGH_COST_VALUE;
        }
        
        // Calcular promedio
        double average = validPrices.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(HIGH_COST_VALUE);
        
        log.debug("Costo promedio calculado de {} servicios: {}", validPrices.size(), average);
        return average;
    }

    /**
     * Extrae categorías (preferencias) únicas de todos los servicios del proveedor
     * Las categorías se obtienen desde el microservicio de preferencias
     * 
     * @param services Lista de servicios
     * @return Lista de nombres de categorías únicas
     */
    private List<String> extractCategoriesFromServices(List<ServicioDTO> services) {
        if (services == null || services.isEmpty()) {
            log.debug("No hay servicios, retornando lista vacía de categorías");
            return new ArrayList<>();
        }
        
        List<String> allCategories = new ArrayList<>();
        
        for (ServicioDTO service : services) {
            try {
                // Obtener preferencias asociadas al servicio
                List<ServicioXPreferenciaDTO> preferences = 
                    preferenciasApiClient.obtenerPreferenciasPorServicio(service.getIdServicio());
                
                if (preferences != null && !preferences.isEmpty()) {
                    // Extraer IDs de preferencias y filtrar nulls
                    List<Long> preferenceIds = preferences.stream()
                        .map(ServicioXPreferenciaDTO::getIdPreferencia)
                        .filter(id -> id != null) // Filtrar IDs null
                        .collect(Collectors.toList());
                    
                    log.debug("Servicio {} tiene {} preferencias", 
                        service.getNombre(), preferenceIds.size());
                    
                    // Aquí podrías hacer una llamada adicional para obtener los nombres
                    // de las preferencias si el endpoint lo permite
                    // Por ahora, usamos los IDs como strings
                    preferenceIds.forEach(id -> allCategories.add("Preferencia_" + id));
                }
                
            } catch (Exception e) {
                log.warn("Error obteniendo preferencias para servicio {}: {}", 
                    service.getIdServicio(), e.getMessage());
            }
        }
        
        // Retornar lista única de categorías
        List<String> uniqueCategories = allCategories.stream()
            .distinct()
            .collect(Collectors.toList());
        
        log.debug("Total de categorías únicas encontradas: {}", uniqueCategories.size());
        return uniqueCategories;
    }

    /**
     * Obtiene datos enriquecidos de múltiples proveedores
     * 
     * @param providerIds Lista de IDs de proveedores
     * @return Mapa de providerId -> datos enriquecidos
     */
    public Map<Long, EnrichedProviderData> getEnrichedProviderDataBatch(List<Long> providerIds) {
        log.info("Obteniendo datos enriquecidos para {} proveedores", providerIds.size());
        
        Map<Long, EnrichedProviderData> enrichedDataMap = new HashMap<>();
        
        for (Long providerId : providerIds) {
            try {
                EnrichedProviderData enrichedData = getEnrichedProviderData(providerId);
                enrichedDataMap.put(providerId, enrichedData);
            } catch (Exception e) {
                log.error("Error procesando proveedor {}: {}", providerId, e.getMessage());
                // Continuar con el siguiente proveedor
            }
        }
        
        log.info("Datos enriquecidos obtenidos para {}/{} proveedores", 
            enrichedDataMap.size(), providerIds.size());
        
        return enrichedDataMap;
    }

    /**
     * Obtiene el costo promedio de un proveedor
     * Método de conveniencia para obtener solo el costo
     * 
     * @param providerId ID del proveedor
     * @return Costo promedio o HIGH_COST_VALUE si no tiene servicios válidos
     */
    public Double getProviderAverageCost(Long providerId) {
        try {
            List<ServicioDTO> services = servicioApiClient.obtenerServiciosPorProveedor(providerId);
            return calculateAverageCost(services);
        } catch (Exception e) {
            log.error("Error calculando costo promedio para proveedor {}: {}", 
                providerId, e.getMessage());
            return HIGH_COST_VALUE;
        }
    }

    /**
     * Obtiene las categorías de un proveedor
     * Método de conveniencia para obtener solo las categorías
     * 
     * @param providerId ID del proveedor
     * @return Lista de categorías
     */
    public List<String> getProviderCategories(Long providerId) {
        try {
            List<ServicioDTO> services = servicioApiClient.obtenerServiciosPorProveedor(providerId);
            return extractCategoriesFromServices(services);
        } catch (Exception e) {
            log.error("Error obteniendo categorías para proveedor {}: {}", 
                providerId, e.getMessage());
            return new ArrayList<>();
        }
    }
}
