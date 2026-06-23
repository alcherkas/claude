package com.quickbite.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pricingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite Pricing Service")
                        .description("Stateless quote engine: prices a cart against menu-service "
                                + "and promotion-service, then applies delivery, service and tax fees.")
                        .version("1.0.0")
                        .contact(new Contact().name("QuickBite Platform").email("platform@quickbite.example"))
                        .license(new License().name("Apache-2.0")));
    }
}
