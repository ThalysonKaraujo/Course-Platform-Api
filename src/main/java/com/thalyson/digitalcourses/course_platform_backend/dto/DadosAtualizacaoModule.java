package com.thalyson.digitalcourses.course_platform_backend.dto;

import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.UniqueModuleOrderIndexInCourse;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.UniqueModuleTitleInCourse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para a atualização de um módulo existente")
public record DadosAtualizacaoModule(
        @NotBlank(message = "O título do módulo não pode ser em branco.")
        @Size(max = 255,  message = "O título não pode exceder {max} caracteres.")
        @UniqueModuleTitleInCourse
        @Schema(description = "Novo título do módulo (máximo 255 caracteres, deve ser único no curso)", example = "Módulo 1: Java Básico", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Size(max = 500, message = "A descrição não pode exceder {max} caracteres.")
        @Schema(description = "Nova descrição do módulo (máximo 500 caracteres)", example = "Este módulo foca nos conceitos introdutórios da linguagem Java.")
        String description,
        @Min(value = 1, message = "A ordem do módulo deve ser pelo menos 1.")
        @UniqueModuleOrderIndexInCourse
        @Schema(description = "Novo índice de ordenação do módulo (mínimo 1, deve ser único no curso)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer orderIndex
) {
}