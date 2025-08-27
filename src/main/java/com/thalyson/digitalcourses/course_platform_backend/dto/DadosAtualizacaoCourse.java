package com.thalyson.digitalcourses.course_platform_backend.dto;

import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingCategory;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingIntructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para a atualização de um curso existente")
public record DadosAtualizacaoCourse(
        @NotBlank
        @Size(min = 5, max = 100)
        @Pattern(regexp = "^[\\p{L}0-9 .,!?]+$", message = "{course.title.pattern}")
        @Schema(description = "Novo título do curso (entre 5 e 100 caracteres)", example = "Tópicos Avançados em Java", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Size(max = 500)
        @NotBlank
        @Schema(description = "Nova descrição do curso (máximo 500 caracteres)", example = "Uma visão aprofundada de conceitos avançados da linguagem Java.")
        String description,

        @Schema(description = "Nova URL da imagem de miniatura do curso", example = "https://example.com/images/java-advanced.png")
        String thumbnailUrl,

        @Schema(description = "Novo status de publicação do curso", example = "false")
        Boolean isPublished,

        @NotNull
        @ExistingIntructor
        @Schema(description = "ID do novo instrutor responsável pelo curso", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Long instructorId,

        @NotNull
        @ExistingCategory
        @Schema(description = "ID da nova categoria à qual o curso pertence", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer categoryId
) {
}