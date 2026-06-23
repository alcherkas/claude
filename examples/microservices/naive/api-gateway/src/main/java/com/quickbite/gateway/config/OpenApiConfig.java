package com.quickbite.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the gateway itself. Individual service contracts live in each backend repo;
 * this document describes the routing edge and its auth rules.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite API Gateway")
                        .description("Reactive edge routing /api/** to backend services. Validates the "
                                + "shared HS256 JWT for protected routes and forwards X-User-Id / "
                                + "X-User-Role headers downstream. /internal/** is never exposed.")
                        .version("1.0.0")
                        .contact(new Contact().name("QuickBite Platform").email("platform@quickbite.example"))
                        .license(new License().name("Apache-2.0")));
    }
}
