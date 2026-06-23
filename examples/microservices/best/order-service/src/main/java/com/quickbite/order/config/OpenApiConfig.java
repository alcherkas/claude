package com.quickbite.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite order-service API")
                        .version("1.0.0")
                        .description("Central checkout hub: creates orders from a cart snapshot, "
                                + "prices them, persists a pricing snapshot, and emits order events.")
                        .license(new License().name("Apache-2.0")));
    }
}
