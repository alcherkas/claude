package com.quickbite.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI searchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuickBite Search Service API")
                        .description("Denormalized search read model built from restaurant.events and "
                                + "menu.events. Public discovery queries over restaurants and menu items.")
                        .version("1.0.0")
                        .license(new License().name("Proprietary")));
    }
}
