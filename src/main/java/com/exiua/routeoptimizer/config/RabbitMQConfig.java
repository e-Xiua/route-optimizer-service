package com.exiua.routeoptimizer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ integrada con arquitectura de microservicios existente
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // === ROUTE OPTIMIZATION EXCHANGES ===
    
    @Bean
    public TopicExchange routeOptimizationExchange() {
        return ExchangeBuilder.topicExchange("route.optimization.exchange")
                .durable(true)
                .build();
    }
    
    // === ROUTE OPTIMIZATION QUEUES ===
    
    @Bean
    public Queue routeOptimizationRequestQueue() {
        return QueueBuilder.durable("route.optimization.request.queue")
                .withArgument("x-message-ttl", 3600000) // 1 hora TTL
                .withArgument("x-max-length", 1000)
                .build();
    }
    
    @Bean
    public Queue routeOptimizationProgressQueue() {
        return QueueBuilder.durable("route.optimization.progress.queue")
                .withArgument("x-message-ttl", 600000) // 10 minutos TTL
                .withArgument("x-max-length", 5000)
                .build();
    }
    
    @Bean
    public Queue routeOptimizationCompletedQueue() {
        return QueueBuilder.durable("route.optimization.completed.queue")
                .withArgument("x-message-ttl", 86400000) // 24 horas TTL
                .withArgument("x-max-length", 2000)
                .build();
    }
    
    @Bean
    public Queue routeOptimizationFailedQueue() {
        return QueueBuilder.durable("route.optimization.failed.queue")
                .withArgument("x-message-ttl", 86400000) // 24 horas TTL
                .withArgument("x-max-length", 1000)
                .build();
    }
    
    // === INTEGRATION QUEUES (Para comunicación con otros servicios) ===
    
    @Bean
    public Queue userPreferencesIntegrationQueue() {
        return QueueBuilder.durable("route.user.preferences.integration.queue")
                .withArgument("x-message-ttl", 1800000) // 30 minutos TTL
                .build();
    }
    
    @Bean
    public Queue providerServicesIntegrationQueue() {
        return QueueBuilder.durable("route.provider.services.integration.queue")
                .withArgument("x-message-ttl", 1800000) // 30 minutos TTL
                .build();
    }
    
    @Bean
    public Queue userActivityIntegrationQueue() {
        return QueueBuilder.durable("route.user.activity.integration.queue")
                .withArgument("x-message-ttl", 1800000) // 30 minutos TTL
                .build();
    }
    
    // === BINDINGS ===
    
    @Bean
    public Binding routeOptimizationRequestBinding() {
        return BindingBuilder.bind(routeOptimizationRequestQueue())
                .to(routeOptimizationExchange())
                .with("route.optimization.request.#");
    }
    
    @Bean
    public Binding routeOptimizationProgressBinding() {
        return BindingBuilder.bind(routeOptimizationProgressQueue())
                .to(routeOptimizationExchange())
                .with("route.optimization.progress.#");
    }
    
    @Bean
    public Binding routeOptimizationCompletedBinding() {
        return BindingBuilder.bind(routeOptimizationCompletedQueue())
                .to(routeOptimizationExchange())
                .with("route.optimization.completed.#");
    }
    
    @Bean
    public Binding routeOptimizationFailedBinding() {
        return BindingBuilder.bind(routeOptimizationFailedQueue())
                .to(routeOptimizationExchange())
                .with("route.optimization.failed.#");
    }
    
    // === INTEGRATION BINDINGS (Para conectar con otros microservicios) ===
    
    @Bean
    public Binding userPreferencesIntegrationBinding() {
        return BindingBuilder.bind(userPreferencesIntegrationQueue())
                .to(routeOptimizationExchange())
                .with("route.integration.preferences.#");
    }
    
    @Bean
    public Binding providerServicesIntegrationBinding() {
        return BindingBuilder.bind(providerServicesIntegrationQueue())
                .to(routeOptimizationExchange())
                .with("route.integration.services.#");
    }
    
    @Bean
    public Binding userActivityIntegrationBinding() {
        return BindingBuilder.bind(userActivityIntegrationQueue())
                .to(routeOptimizationExchange())
                .with("route.integration.activity.#");
    }
    
    // === RABBIT TEMPLATE CONFIGURATION ===
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.printf("Message failed to deliver: %s%n", cause);
            }
        });
        return template;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(5);
        return factory;
    }
    
    // === DEAD LETTER QUEUE CONFIGURATION ===
    
    @Bean
    public Queue routeOptimizationDeadLetterQueue() {
        return QueueBuilder.durable("route.optimization.dlq").build();
    }
    
    @Bean
    public DirectExchange routeOptimizationDeadLetterExchange() {
        return ExchangeBuilder.directExchange("route.optimization.dlx").build();
    }
    
    @Bean
    public Binding routeOptimizationDeadLetterBinding() {
        return BindingBuilder.bind(routeOptimizationDeadLetterQueue())
                .to(routeOptimizationDeadLetterExchange())
                .with("failed");
    }
}