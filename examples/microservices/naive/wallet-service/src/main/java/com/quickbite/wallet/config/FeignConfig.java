package com.quickbite.wallet.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Feign configuration for outbound calls to dependency services
 * (identity-service). Cross-service calls only ever target the dependency's
 * /internal/** endpoints.
 */
@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
