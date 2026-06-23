package com.quickbite.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite payment-service API")
                        .version("1.0.0")
                        .description("Captures order payments via wallet debit or a mock card PSP, "
                                + "supports refunds, and emits payment events.")
                        .license(new License().name("Apache-2.0")));
    }
}
