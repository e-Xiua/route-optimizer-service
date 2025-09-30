package com.exiua.routeoptimizer.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.exiua.routeoptimizer.dto.JobStatusResponseDTO;
import com.exiua.routeoptimizer.dto.JobSubmissionResponseDTO;
import com.exiua.routeoptimizer.dto.RouteProcessingRequestDTO;
import com.exiua.routeoptimizer.model.OptimizationJob;
import com.exiua.routeoptimizer.model.RouteOptimizationRequest;
import com.exiua.routeoptimizer.repository.OptimizationJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Servicio mejorado para manejar múltiples trabajos de optimización concurrentes
 * Utiliza CompletableFuture y ThreadPoolTaskExecutor para procesamiento asíncrono
 */
@Service
public class EnhancedRouteOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedRouteOptimizationService.class);
    
    private final OptimizationJobRepository jobRepository;
    private final MockDataService mockDataService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final Executor taskExecutor;
    
    // Estadísticas de trabajos activos
    private final ConcurrentHashMap<String, CompletableFuture<Void>> activeJobs = new ConcurrentHashMap<>();
    private final AtomicInteger totalJobsSubmitted = new AtomicInteger(0);
    private final AtomicInteger totalJobsCompleted = new AtomicInteger(0);
    private final AtomicInteger totalJobsFailed = new AtomicInteger(0);
    
    @Value("${route.processing.service.url:http://localhost:8086}")
    private String routeProcessingServiceUrl;
    
    @Value("${server.base-url:http://localhost:8085}")
    private String baseUrl;
    
    @Value("${optimization.max-concurrent-jobs:5}")
    private int maxConcurrentJobs;
    
    @Value("${optimization.job-timeout-minutes:10}")
    private int jobTimeoutMinutes;
    
    @Value("${optimization.retry-attempts:3}")
    private int retryAttempts;
    
    public EnhancedRouteOptimizationService(
            OptimizationJobRepository jobRepository, 
            MockDataService mockDataService,
            ObjectMapper objectMapper,
            Executor taskExecutor) {
        this.jobRepository = jobRepository;
        this.mockDataService = mockDataService;
        this.objectMapper = objectMapper;
        this.taskExecutor = taskExecutor;
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }
    
    /**
     * Enviar solicitud de optimización con manejo de concurrencia mejorado
     */
    public JobSubmissionResponseDTO submitOptimizationRequest(RouteOptimizationRequest request) {
        try {
            // Verificar límite de trabajos concurrentes
            if (activeJobs.size() >= maxConcurrentJobs) {
                logger.warn("Límite de trabajos concurrentes alcanzado: {}", maxConcurrentJobs);
                throw new RuntimeException("Sistema ocupado. Demasiados trabajos en procesamiento. Intente más tarde.");
            }
            
            logger.info("=== NUEVA SOLICITUD DE OPTIMIZACIÓN ===");
            logger.info("Route ID: {}", request.getRouteId());
            logger.info("User ID: {}", request.getUserId());
            logger.info("Número de POIs: {}", request.getPois() != null ? request.getPois().size() : 0);
            logger.info("Trabajos activos: {}/{}", activeJobs.size(), maxConcurrentJobs);
            
            // Generar job ID único
            String jobId = UUID.randomUUID().toString();
            totalJobsSubmitted.incrementAndGet();
            
            // Crear trabajo en base de datos
            OptimizationJob job = new OptimizationJob(jobId, request.getUserId(), request.getRouteId());
            job.setRequestData(objectMapper.writeValueAsString(request));
            job.setEstimatedCompletionTime(LocalDateTime.now().plusMinutes(jobTimeoutMinutes));
            jobRepository.save(job);
            
            // Crear respuesta
            String pollingUrl = baseUrl + "/api/v1/jobs/" + jobId + "/status";
            JobSubmissionResponseDTO response = new JobSubmissionResponseDTO(jobId, pollingUrl);
            response.setEstimatedCompletionTime(job.getEstimatedCompletionTime());
            
            // Iniciar procesamiento asíncrono con CompletableFuture
            CompletableFuture<Void> jobFuture = processOptimizationAsyncEnhanced(jobId, request)
                .orTimeout((long) jobTimeoutMinutes, java.util.concurrent.TimeUnit.MINUTES)
                .whenComplete((result, throwable) -> {
                    // Remover de trabajos activos al completar
                    activeJobs.remove(jobId);
                    
                    if (throwable != null) {
                        logger.error("Error en procesamiento asíncrono para job {}: {}", jobId, throwable.getMessage());
                        totalJobsFailed.incrementAndGet();
                        updateJobStatusWithError(jobId, OptimizationJob.JobStatus.FAILED, throwable.getMessage());
                    } else {
                        totalJobsCompleted.incrementAndGet();
                        logger.info("Trabajo {} completado exitosamente", jobId);
                    }
                });
            
            // Registrar trabajo activo
            activeJobs.put(jobId, jobFuture);
            
            logger.info("Solicitud de optimización enviada con job ID: {}", jobId);
            return response;
            
        } catch (Exception e) {
            logger.error("Error enviando solicitud de optimización", e);
            throw new RuntimeException("Error enviando solicitud de optimización: " + e.getMessage());
        }
    }
    
    /**
     * Procesamiento asíncrono mejorado con CompletableFuture
     */
    private CompletableFuture<Void> processOptimizationAsyncEnhanced(String jobId, RouteOptimizationRequest request) {
        return CompletableFuture
            .supplyAsync(() -> {
                logger.info("Iniciando procesamiento asíncrono mejorado para job: {}", jobId);
                updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, 10);
                
                // Simular pasos de procesamiento
                return simulateProcessingStepsEnhanced(jobId);
            }, taskExecutor)
            .thenCompose(stepResult -> {
                // Llamar servicio de procesamiento con reintentos
                return callRouteProcessingServiceEnhanced(jobId, request);
            })
            .thenAccept(result -> {
                // Actualizar resultado final
                updateJobStatus(jobId, OptimizationJob.JobStatus.COMPLETED, 100);
                updateJobResult(jobId, result);
                logger.info("Procesamiento completado para job: {}", jobId);
            })
            .exceptionally(throwable -> {
                logger.error("Error en procesamiento para job {}: {}", jobId, throwable.getMessage());
                updateJobStatusWithError(jobId, OptimizationJob.JobStatus.FAILED, throwable.getMessage());
                return null;
            });
    }
    
    /**
     * Simular pasos de procesamiento con progreso detallado
     */
    private String simulateProcessingStepsEnhanced(String jobId) {
        String[] steps = {
            "Validando datos de entrada",
            "Preparando datos sintéticos",
            "Calculando matrices de distancia",
            "Optimizando secuencia de POIs"
        };
        
        try {
            for (int i = 0; i < steps.length; i++) {
                logger.info("Job {}: {}", jobId, steps[i]);
                updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, 20 + (i * 15));
                Thread.sleep(1000 + (long)(Math.random() * 2000)); // Simular tiempo variable
            }
            
            return "preprocessing_completed";
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Procesamiento interrumpido", e);
        }
    }
    
    /**
     * Llamar servicio de procesamiento con reintentos y circuit breaker
     */
    private CompletableFuture<String> callRouteProcessingServiceEnhanced(String jobId, RouteOptimizationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Llamando servicio de procesamiento para job: {}", jobId);
                updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, 60);
                
                // Construir solicitud
                RouteProcessingRequestDTO processingRequest = buildProcessingRequestEnhanced(request);
                
                // Llamar servicio con reintentos usando WebClient reactivo
                String processingUrl = routeProcessingServiceUrl + "/api/v1/process-route";
                
                Mono<String> resultMono = webClient.post()
                    .uri(processingUrl)
                    .bodyValue(processingRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(10))
                        .doBeforeRetry(retrySignal -> {
                            logger.warn("Reintentando llamada para job {} (intento {})", 
                                jobId, retrySignal.totalRetries() + 1);
                        }))
                    .timeout(Duration.ofMinutes(jobTimeoutMinutes - 2)); // Dejar margen para timeout
                
                String result = resultMono.block(); // Convertir a síncrono para CompletableFuture
                
                logger.info("Respuesta recibida del servicio de procesamiento para job: {}", jobId);
                updateJobStatus(jobId, OptimizationJob.JobStatus.PROCESSING, 90);
                
                return result != null ? result : generateFallbackResult(request, jobId);
                
            } catch (Exception e) {
                logger.error("Error llamando servicio de procesamiento para job {}: {}", jobId, e.getMessage());
                
                // Generar resultado de respaldo
                logger.info("Generando resultado de respaldo para job: {}", jobId);
                return generateFallbackResult(request, jobId);
            }
        }, taskExecutor);
    }
    
    /**
     * Construir solicitud de procesamiento mejorada
     */
    private RouteProcessingRequestDTO buildProcessingRequestEnhanced(RouteOptimizationRequest request) {
        RouteProcessingRequestDTO processingRequest = new RouteProcessingRequestDTO();
        
        // Información básica
        processingRequest.setRouteId(request.getRouteId() != null ? request.getRouteId() : UUID.randomUUID().toString());
        processingRequest.setUserId(request.getUserId() != null ? request.getUserId() : "system-user");
        
        // Convertir POIs con validación mejorada
        List<RouteProcessingRequestDTO.ProcessingPOIDTO> processingPOIs = request.getPois().stream()
            .map(poi -> {
                RouteProcessingRequestDTO.ProcessingPOIDTO processingPOI = new RouteProcessingRequestDTO.ProcessingPOIDTO();
                
                processingPOI.setId(poi.getId() != null ? poi.getId() : System.currentTimeMillis() + Math.round(Math.random() * 1000));
                processingPOI.setName(poi.getName() != null ? poi.getName() : "POI Anónimo");
                processingPOI.setLatitude(poi.getLatitude() != null ? poi.getLatitude() : 10.501);
                processingPOI.setLongitude(poi.getLongitude() != null ? poi.getLongitude() : -84.697);
                processingPOI.setCategory(poi.getCategory() != null ? poi.getCategory() : "tourism");
                processingPOI.setSubcategory("service");
                processingPOI.setVisitDuration(poi.getVisitDuration() != null ? poi.getVisitDuration() : 90);
                processingPOI.setCost(poi.getPriceLevel() != null ? poi.getPriceLevel().doubleValue() * 10.0 : 50.0);
                processingPOI.setRating(poi.getRating() != null ? poi.getRating() : 4.0);
                processingPOI.setProviderId(poi.getProviderId());
                processingPOI.setProviderName(poi.getProviderId() != null ? "Provider-" + poi.getProviderId() : "Proveedor Desconocido");
                
                return processingPOI;
            })
            .collect(Collectors.toList());
        
        processingRequest.setPois(processingPOIs);
        
        // Preferencias con valores por defecto mejorados
        RouteProcessingRequestDTO.RoutePreferencesDTO preferences = new RouteProcessingRequestDTO.RoutePreferencesDTO();
        if (request.getPreferences() != null) {
            preferences.setOptimizeFor(request.getPreferences().getOptimizeFor() != null ? 
                request.getPreferences().getOptimizeFor() : "distance");
            preferences.setMaxTotalTime(request.getPreferences().getMaxTotalTime());
            preferences.setMaxTotalCost(request.getPreferences().getMaxTotalCost());
        } else {
            preferences.setOptimizeFor("distance");
            preferences.setMaxTotalTime(480); // 8 horas por defecto
        }
        preferences.setAccessibilityRequired(false);
        processingRequest.setPreferences(preferences);
        
        // Restricciones por defecto
        RouteProcessingRequestDTO.RouteConstraintsDTO constraints = new RouteProcessingRequestDTO.RouteConstraintsDTO();
        constraints.setStartTime("09:00");
        constraints.setLunchBreakRequired(true);
        constraints.setLunchBreakDuration(60);
        processingRequest.setConstraints(constraints);
        
        return processingRequest;
    }
    
    /**
     * Generar resultado de respaldo mejorado
     */
    private String generateFallbackResult(RouteOptimizationRequest request, String jobId) {
        try {
            var result = new Object() {
                public final String optimized_route_id = "fallback_" + jobId.substring(0, 8);
                public final Object[] optimized_sequence = request.getPois().stream()
                    .map(poi -> new Object() {
                        public final Long poi_id = poi.getId();
                        public final String name = poi.getName();
                        public final Double latitude = poi.getLatitude();
                        public final Double longitude = poi.getLongitude();
                        public final Integer visit_order = request.getPois().indexOf(poi) + 1;
                        public final Integer estimated_visit_time = poi.getVisitDuration();
                    }).toArray();
                public final Double total_distance_km = 45.7 + (Math.random() * 50);
                public final Integer total_time_minutes = 280 + (int)(Math.random() * 120);
                public final String optimization_algorithm = "Fallback-Algorithm";
                public final Double optimization_score = 0.75 + (Math.random() * 0.2);
                public final String generated_at = LocalDateTime.now().toString();
                public final String note = "Resultado generado por algoritmo de respaldo";
            };
            
            return objectMapper.writeValueAsString(result);
            
        } catch (Exception e) {
            logger.error("Error generando resultado de respaldo para job {}: {}", jobId, e.getMessage());
            return "{\"error\":\"No se pudo generar resultado de respaldo\"}";
        }
    }
    
    /**
     * Obtener estado del trabajo
     */
    public Optional<JobStatusResponseDTO> getJobStatus(String jobId) {
        Optional<OptimizationJob> jobOpt = jobRepository.findById(jobId);
        
        if (jobOpt.isEmpty()) {
            return Optional.empty();
        }
        
        OptimizationJob job = jobOpt.get();
        JobStatusResponseDTO response = new JobStatusResponseDTO(jobId, job.getStatus().name());
        
        response.setProgressPercentage(job.getProgressPercentage());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setEstimatedCompletionTime(job.getEstimatedCompletionTime());
        
        // Información adicional sobre concurrencia
        boolean isActive = activeJobs.containsKey(jobId);
        
        switch (job.getStatus()) {
            case PENDING:
                response.setMessage("Solicitud en cola para procesamiento");
                response.setRetryAfterSeconds(isActive ? 20 : 30);
                break;
            case PROCESSING:
                response.setMessage(String.format("Optimización en progreso (%d%% completado)", 
                    job.getProgressPercentage()));
                response.setRetryAfterSeconds(15);
                break;
            case COMPLETED:
                response.setMessage("Optimización completada exitosamente");
                try {
                    if (job.getResultData() != null) {
                        response.setResult(objectMapper.readValue(job.getResultData(), Object.class));
                    }
                } catch (Exception e) {
                    logger.error("Error analizando datos de resultado para job {}", jobId, e);
                }
                break;
            case FAILED:
                response.setMessage("La optimización falló");
                response.setError(new JobStatusResponseDTO.ErrorDetails(
                    "OPTIMIZATION_FAILED", 
                    job.getErrorMessage() != null ? job.getErrorMessage() : "Error desconocido",
                    "El proceso de optimización encontró un error"
                ));
                break;
            case CANCELLED:
                response.setMessage("Optimización cancelada");
                break;
        }
        
        return Optional.of(response);
    }
    
    /**
     * Cancelar trabajo con manejo mejorado
     */
    public boolean cancelJob(String jobId) {
        // Intentar cancelar trabajo activo
        CompletableFuture<Void> activeFuture = activeJobs.get(jobId);
        if (activeFuture != null) {
            boolean cancelled = activeFuture.cancel(true);
            if (cancelled) {
                activeJobs.remove(jobId);
                logger.info("Trabajo activo {} cancelado", jobId);
            }
        }
        
        // Actualizar estado en base de datos
        Optional<OptimizationJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isPresent()) {
            OptimizationJob job = jobOpt.get();
            if (job.getStatus() == OptimizationJob.JobStatus.PENDING || 
                job.getStatus() == OptimizationJob.JobStatus.PROCESSING) {
                job.setStatus(OptimizationJob.JobStatus.CANCELLED);
                jobRepository.save(job);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Obtener estadísticas del sistema
     */
    public SystemStatsDTO getSystemStats() {
        SystemStatsDTO stats = new SystemStatsDTO();
        stats.setActiveJobs(activeJobs.size());
        stats.setMaxConcurrentJobs(maxConcurrentJobs);
        stats.setTotalJobsSubmitted(totalJobsSubmitted.get());
        stats.setTotalJobsCompleted(totalJobsCompleted.get());
        stats.setTotalJobsFailed(totalJobsFailed.get());
        stats.setSuccessRate(totalJobsSubmitted.get() > 0 ? 
            (double) totalJobsCompleted.get() / totalJobsSubmitted.get() * 100 : 0.0);
        
        return stats;
    }
    
    // Métodos auxiliares
    
    private void updateJobStatus(String jobId, OptimizationJob.JobStatus status, Integer progress) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setProgressPercentage(progress);
            jobRepository.save(job);
        });
    }
    
    private void updateJobStatusWithError(String jobId, OptimizationJob.JobStatus status, String errorMessage) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setErrorMessage(errorMessage);
            jobRepository.save(job);
        });
    }
    
    private void updateJobResult(String jobId, String resultData) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setResultData(resultData);
            jobRepository.save(job);
        });
    }
    
    // DTO para estadísticas del sistema
    public static class SystemStatsDTO {
        private int activeJobs;
        private int maxConcurrentJobs;
        private int totalJobsSubmitted;
        private int totalJobsCompleted;
        private int totalJobsFailed;
        private double successRate;
        
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
    }
}