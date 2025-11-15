package com.exiua.routeoptimizer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exiua.routeoptimizer.events.RouteOptimizationEvent;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;

/**
 * Servicio para publicar eventos de optimización de rutas en RabbitMQ
 * Integrado con la arquitectura de colas existente
 */
@Service
public class RouteOptimizationEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizationEventPublisher.class);
    
    private static final String ROUTE_OPTIMIZATION_EXCHANGE = "route.optimization.exchange";
    
    @Autowired
    private AmqpTemplate rabbitTemplate;
    
    /**
     * Publicar evento de solicitud de optimización
     */
    public void publishOptimizationRequested(String jobId, String userId, RouteOptimizationRequest request) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(jobId, userId, 
                    RouteOptimizationEvent.EventType.OPTIMIZATION_REQUESTED);
            
            event.setRouteId(request.getRouteId());
            event.setStatus("REQUESTED");
            event.setMessage("Optimización de ruta solicitada");
            event.setPois(convertPOIs(request.getPois()));
            event.setPreferences(convertPreferences(request.getPreferences()));
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE, 
                    "route.optimization.request.new", event);
            
            // También publicar para integración con otros servicios
            publishUserActivityEvent(userId, "ROUTE_OPTIMIZATION_REQUESTED", jobId);
            
            logger.info("Evento OPTIMIZATION_REQUESTED publicado para job: {}", jobId);
            
        } catch (Exception e) {
            logger.error("Error publicando evento OPTIMIZATION_REQUESTED para job {}: {}", jobId, e.getMessage());
        }
    }
    
    /**
     * Publicar evento de inicio de optimización
     */
    public void publishOptimizationStarted(OptimizationJob job) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(job.getJobId(), job.getUserId(),
                    RouteOptimizationEvent.EventType.OPTIMIZATION_STARTED);
            
            event.setRouteId(job.getRouteId());
            event.setStatus("PROCESSING");
            event.setProgress(0);
            event.setMessage("Procesamiento de optimización iniciado");
            event.setEstimatedCompletion(job.getEstimatedCompletionTime());
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.optimization.progress.started", event);
            
            logger.info("Evento OPTIMIZATION_STARTED publicado para job: {}", job.getJobId());
            
        } catch (Exception e) {
            logger.error("Error publicando evento OPTIMIZATION_STARTED para job {}: {}", job.getJobId(), e.getMessage());
        }
    }
    
    /**
     * Publicar evento de progreso de optimización
     */
    public void publishOptimizationProgress(String jobId, String userId, int progress, String message) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(jobId, userId,
                    RouteOptimizationEvent.EventType.OPTIMIZATION_PROGRESS);
            
            event.setStatus("PROCESSING");
            event.setProgress(progress);
            event.setMessage(message);
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.optimization.progress.update", event);
            
            logger.debug("Evento OPTIMIZATION_PROGRESS publicado para job: {} - {}%", jobId, progress);
            
        } catch (Exception e) {
            logger.error("Error publicando evento OPTIMIZATION_PROGRESS para job {}: {}", jobId, e.getMessage());
        }
    }
    
    /**
     * Publicar evento de optimización completada
     */
    public void publishOptimizationCompleted(OptimizationJob job) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(job.getJobId(), job.getUserId(),
                    RouteOptimizationEvent.EventType.OPTIMIZATION_COMPLETED);
            
            event.setRouteId(job.getRouteId());
            event.setStatus("COMPLETED");
            event.setProgress(100);
            event.setMessage("Optimización completada exitosamente");
            
            // Agregar resultado si está disponible
            if (job.getResult() != null) {
                RouteOptimizationEvent.OptimizationResultData result = 
                        new RouteOptimizationEvent.OptimizationResultData();
                // Usar el routeId real como identificador de la ruta optimizada (no el jobId)
                result.setOptimizedRouteId(job.getRouteId() != null ? job.getRouteId() : job.getJobId());
                result.setRouteDescription("Ruta optimizada usando algoritmo MRL-AMIS");
                // Aquí se podrían mapear más datos del resultado
                event.setResult(result);
            }
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.optimization.completed.success", event);
            
            // Publicar para actualizar preferencias del usuario
            publishUserPreferencesUpdate(job.getUserId(), job.getJobId());
            
            // Publicar actividad del usuario
            publishUserActivityEvent(job.getUserId(), "ROUTE_OPTIMIZATION_COMPLETED", job.getJobId());
            
            logger.info("Evento OPTIMIZATION_COMPLETED publicado para job: {}", job.getJobId());
            
        } catch (Exception e) {
            logger.error("Error publicando evento OPTIMIZATION_COMPLETED para job {}: {}", job.getJobId(), e.getMessage());
        }
    }
    
    /**
     * Publicar evento de optimización fallida
     */
    public void publishOptimizationFailed(String jobId, String userId, String errorMessage) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(jobId, userId,
                    RouteOptimizationEvent.EventType.OPTIMIZATION_FAILED);
            
            event.setStatus("FAILED");
            event.setMessage("Error en optimización: " + errorMessage);
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.optimization.failed.error", event);
            
            // Publicar actividad del usuario
            publishUserActivityEvent(userId, "ROUTE_OPTIMIZATION_FAILED", jobId);
            
            logger.warn("Evento OPTIMIZATION_FAILED publicado para job: {} - Error: {}", jobId, errorMessage);
            
        } catch (Exception e) {
            logger.error("Error publicando evento OPTIMIZATION_FAILED para job {}: {}", jobId, e.getMessage());
        }
    }
    
    /**
     * Publicar evento de optimización cancelada
     */
    public void publishOptimizationCancelled(String jobId, String userId) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(jobId, userId,
                    RouteOptimizationEvent.EventType.OPTIMIZATION_CANCELLED);
            
            event.setStatus("CANCELLED");
            event.setMessage("Optimización cancelada por el usuario");
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.optimization.failed.cancelled", event);
            
            logger.info("Evento OPTIMIZATION_CANCELLED publicado para job: {}", jobId);
            
        } catch (Exception e) {
            logger.error("Error publicando evento OPTIMIZATION_CANCELLED para job {}: {}", jobId, e.getMessage());
        }
    }
    
    /**
     * Publicar evento para actualizar preferencias del usuario
     */
    private void publishUserPreferencesUpdate(String userId, String jobId) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(jobId, userId,
                    RouteOptimizationEvent.EventType.OPTIMIZATION_COMPLETED);
            
            event.setMessage("Actualizar preferencias basadas en optimización completada");
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.integration.preferences.update", event);
            
            logger.debug("Evento de integración de preferencias publicado para usuario: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error publicando evento de integración de preferencias: {}", e.getMessage());
        }
    }
    
    /**
     * Publicar evento de actividad del usuario
     */
    private void publishUserActivityEvent(String userId, String activity, String jobId) {
        try {
            RouteOptimizationEvent event = new RouteOptimizationEvent(jobId, userId,
                    RouteOptimizationEvent.EventType.OPTIMIZATION_REQUESTED);
            
            event.setMessage(activity);
            
            rabbitTemplate.convertAndSend(ROUTE_OPTIMIZATION_EXCHANGE,
                    "route.integration.activity.user", event);
            
            logger.debug("Evento de actividad de usuario publicado: {} - {}", userId, activity);
            
        } catch (Exception e) {
            logger.error("Error publicando evento de actividad de usuario: {}", e.getMessage());
        }
    }
    
    /**
     * Convertir POIs a formato de evento
     */
    private List<RouteOptimizationEvent.POIData> convertPOIs(List<com.exiua.routeoptimizer.model.POI> pois) {
        return pois.stream().map(poi -> {
            RouteOptimizationEvent.POIData poiData = new RouteOptimizationEvent.POIData();
            poiData.setId(poi.getId());
            poiData.setName(poi.getName());
            poiData.setLatitude(poi.getLatitude());
            poiData.setLongitude(poi.getLongitude());
            poiData.setCategories(poi.getCategories());
            poiData.setSubcategory(poi.getSubcategory());
            poiData.setVisitDuration(poi.getVisitDuration());
            poiData.setCost(poi.getPriceLevel() != null ? Double.valueOf(poi.getPriceLevel()) : null);
            poiData.setRating(poi.getRating());
            return poiData;
        }).collect(Collectors.toList());
    }
    
    /**
     * Convertir preferencias a formato de evento
     */
    private RouteOptimizationEvent.RoutePreferencesData convertPreferences(
            RouteOptimizationRequest.RoutePreferences preferences) {
        RouteOptimizationEvent.RoutePreferencesData preferencesData = 
                new RouteOptimizationEvent.RoutePreferencesData();
        
        if (preferences != null) {
            preferencesData.setOptimizeFor(preferences.getOptimizeFor());
            preferencesData.setMaxTotalTime(preferences.getMaxTotalTime());
            preferencesData.setMaxTotalCost(preferences.getMaxTotalCost());
            preferencesData.setAccessibilityRequired(true);
        }
        
        return preferencesData;
    }
}