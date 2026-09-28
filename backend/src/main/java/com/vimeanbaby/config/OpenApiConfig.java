package com.vimeanbaby.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vimeanBabyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vimean Baby API")
                        .description("Mother and baby e-commerce API for Cambodia")
                        .version("0.0.1")
                        .contact(new Contact().name("Vimean Baby")));
    }
}
