package com.thalyson.digitalcourses.course_platform_backend.module.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para o cadastro de um novo módulo em um curso")
public record DadosCadastroModule(
        @Schema(description = "Título do módulo", example = "Módulo 1: Fundamentos", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank
        String title,
        @Schema(description = "Descrição detalhada do módulo", example = "Este módulo abrange os conceitos básicos do curso.")
                @NotBlank
        String description,
        @Schema(description = "Índice de ordenação do módulo dentro do curso", example = "1")
                @NotNull
        Integer orderIndex) {}