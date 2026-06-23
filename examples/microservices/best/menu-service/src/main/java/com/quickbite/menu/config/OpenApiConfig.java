package com.quickbite.menu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI menuOpenApi() {
        return new OpenAPI().info(new Info()
                .title("QuickBite Menu Service API")
                .version("1.0.0")
                .description("Manages restaurant menu items. Public endpoints live under /api/menu; "
                        + "service-to-service endpoints under /internal/menu-items.")
                .contact(new Contact().name("QuickBite Platform").email("platform@quickbite.example"))
                .license(new License().name("Apache-2.0")));
    }
}
