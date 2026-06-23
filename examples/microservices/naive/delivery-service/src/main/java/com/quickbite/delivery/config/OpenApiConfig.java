package com.quickbite.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite delivery-service API")
                        .version("1.0.0")
                        .description("Creates deliveries for ready orders, assigns an available driver, "
                                + "tracks courier location, and emits DeliveryStatusChanged on deliveries.events.")
                        .license(new License().name("Apache-2.0")));
    }
}
