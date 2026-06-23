package com.quickbite.menu.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Feign configuration for outbound calls (currently only restaurant-service).
 * Circuit breaking is provided by spring-cloud-starter-circuitbreaker-resilience4j,
 * enabled via {@code spring.cloud.openfeign.circuitbreaker.enabled} in application.yml.
 */
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
