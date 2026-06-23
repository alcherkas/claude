package com.quickbite.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite Notification Service API")
                        .description("Event-driven notifications. Consumes orders.events, payments.events and "
                                + "deliveries.events, renders templates and (mock) sends them. Exposes a read API "
                                + "for a user's notification history.")
                        .version("1.0.0")
                        .license(new License().name("Proprietary")));
    }
}
