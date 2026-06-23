package com.quickbite.search.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Feign configuration for outbound calls to dependency services
 * (restaurant-service, menu-service). Cross-service calls only ever target
 * the dependency's {@code /internal/**} endpoints (PLATFORM_SPEC §2.1).
 */
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
