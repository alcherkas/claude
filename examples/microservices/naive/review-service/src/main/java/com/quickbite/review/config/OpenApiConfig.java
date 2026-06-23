package com.quickbite.review.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reviewServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite review-service API")
                        .version("1.0.0")
                        .description("Captures customer ratings and reviews for restaurants and drivers. "
                                + "A review is only accepted once the underlying order is DELIVERED and "
                                + "belongs to the reviewing user (verified against order-service).")
                        .license(new License().name("Apache-2.0")));
    }
}
