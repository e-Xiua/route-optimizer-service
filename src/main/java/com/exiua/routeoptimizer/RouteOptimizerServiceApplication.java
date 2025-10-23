package com.exiua.routeoptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableFeignClients
@ConfigurationPropertiesScan
public class RouteOptimizerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteOptimizerServiceApplication.class, args);
    }
}