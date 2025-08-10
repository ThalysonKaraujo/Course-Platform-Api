package com.thalyson.digitalcourses.course_platform_backend.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {

    @Bean
    public OpenApiCustomizer customizer() {
        return openApi -> openApi.getServers().add(
                new Server().url("https://course-platform-api-production.up.railway.app")
        );
    }
}