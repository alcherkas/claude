package com.quickbite.promotion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI promotionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite Promotion Service API")
                        .description("Promo code management, validation and redemption reservation.")
                        .version("1.0.0")
                        .license(new License().name("Apache-2.0")));
    }
}
