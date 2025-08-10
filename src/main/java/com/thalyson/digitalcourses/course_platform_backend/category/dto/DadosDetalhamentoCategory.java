package com.thalyson.digitalcourses.course_platform_backend.category.dto;

import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de uma categoria")
public record DadosDetalhamentoCategory(
        @Schema(description = "ID da categoria", example = "1")
        Integer id,
        @Schema(description = "Nome da categoria", example = "Tecnologia")
        String name,
        @Schema(description = "Data e hora de criação da categoria", example = "2024-05-15T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização da categoria", example = "2024-05-15T11:30:00")
        LocalDateTime updatedAt
) {
    public DadosDetalhamentoCategory(CategoryJPA category) {
        this(category.getId(), category.getName(), category.getCreatedAt(), category.getUpdatedAt());
    }
}