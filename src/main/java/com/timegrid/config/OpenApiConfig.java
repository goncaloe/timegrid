package com.timegrid.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI timeGridOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TimeGrid API")
                        .description("API da plataforma de registo de horas e ausências")
                        .version("v1"));
    }

}
