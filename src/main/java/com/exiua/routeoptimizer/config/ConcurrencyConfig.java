package com.exiua.routeoptimizer.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración para manejo de concurrencia y hilos
 */
@Configuration
@EnableAsync
public class ConcurrencyConfig {
    
    @Value("${optimization.thread-pool.core-size:4}")
    private int corePoolSize;
    
    @Value("${optimization.thread-pool.max-size:10}")
    private int maxPoolSize;
    
    @Value("${optimization.thread-pool.queue-capacity:50}")
    private int queueCapacity;
    
    @Value("${optimization.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    /**
     * ThreadPoolTaskExecutor para procesamiento asíncrono de optimizaciones
     */
    @Bean(name = "optimizationTaskExecutor")
    public Executor optimizationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("OptimizationWorker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Política de rechazo: CallerRunsPolicy para ejecutar en el hilo principal si el pool está lleno
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        return executor;
    }
    
    /**
     * ThreadPoolTaskExecutor para tareas auxiliares (monitoreo, limpieza, etc.)
     */
    @Bean(name = "utilityTaskExecutor")
    public Executor utilityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("UtilityWorker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        
        return executor;
    }
    
    /**
     * Configuración de WebClient para llamadas no bloqueantes
     */
    @Bean
    public org.springframework.web.reactive.function.client.WebClient webClient() {
        return org.springframework.web.reactive.function.client.WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }
}