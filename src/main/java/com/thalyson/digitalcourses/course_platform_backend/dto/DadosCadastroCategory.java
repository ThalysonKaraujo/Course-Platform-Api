package com.thalyson.digitalcourses.course_platform_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "Dados para o cadastro de uma nova categoria")
public record DadosCadastroCategory(
        @NotBlank
        @Schema(description = "Nome da categoria. Deve ser único.", example = "Tecnologia", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
){}