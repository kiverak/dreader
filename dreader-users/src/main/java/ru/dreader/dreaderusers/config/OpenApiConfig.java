package ru.dreader.dreaderusers.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {

        Components components = new Components()
                .addResponses("BadRequest", new ApiResponse().description("Некорректный запрос"))
                .addResponses("Unauthorized", new ApiResponse().description("Не авторизован"))
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(new Info()
                        .title("DReader Users API")
                        .description("API для управления пользователями")
                        .version("1.0.0"))
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
