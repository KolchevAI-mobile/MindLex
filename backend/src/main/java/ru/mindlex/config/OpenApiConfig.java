package ru.mindlex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI-описание доступно по адресу /swagger-ui.html — удобно для демонстрации CRUD.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI mindLexOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MindLex API")
                        .version("1.0.0")
                        .description("REST-бэкенд приложения для изучения иностранной лексики: "
                                + "CRUD над словарями и словами, игровые механики и интервальное повторение."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
