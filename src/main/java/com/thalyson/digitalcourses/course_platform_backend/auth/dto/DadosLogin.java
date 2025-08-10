package com.thalyson.digitalcourses.course_platform_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de autenticação para login do usuário")
public record DadosLogin(
        @NotBlank
        @Schema(description = "Endereço de e-mail do usuário", example = "usuario@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @NotBlank
        @Schema(description = "Senha do usuário", example = "SenhaSegura123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
){}