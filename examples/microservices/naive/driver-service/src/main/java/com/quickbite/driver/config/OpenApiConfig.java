package com.quickbite.driver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI driverOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite Driver Service API")
                        .description("Manages couriers, their availability, location pings and nearest-driver lookup for the QuickBite marketplace.")
                        .version("1.0.0")
                        .contact(new Contact().name("QuickBite Platform").email("platform@quickbite.example"))
                        .license(new License().name("Apache-2.0")));
    }
}
