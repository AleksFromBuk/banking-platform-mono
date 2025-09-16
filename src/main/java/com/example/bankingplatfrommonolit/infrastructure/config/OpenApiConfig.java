package com.example.bankingplatfrommonolit.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Объявляем Bearer JWT схему — Swagger начнет показывать кнопку Authorize.
 * Вставлять токен нужно БЕЗ префикса "Bearer " — UI добавит его сам.
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
class OpenApiConfig {
    @Bean
    OpenAPI api() {
        return new OpenAPI().info(new Info().title("Bank API").version("v1"));
    }
}
