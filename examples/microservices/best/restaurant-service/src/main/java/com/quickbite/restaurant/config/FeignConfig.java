package com.quickbite.restaurant.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Feign configuration for outbound calls to other QuickBite services.
 * Circuit breaking is provided by spring-cloud-starter-circuitbreaker-resilience4j
 * (enabled via {@code spring.cloud.openfeign.circuitbreaker.enabled=true}).
 */
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
