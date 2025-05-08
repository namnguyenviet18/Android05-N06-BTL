package com.group06.music_app.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Music App API",
                version = "1.0",
                description = "API documentation for the Music App"
        )
)
@Configuration
public class OpenApiConfig {
}
