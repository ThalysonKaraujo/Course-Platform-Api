package com.thalyson.digitalcourses.course_platform_backend.course.dto;

import com.thalyson.digitalcourses.course_platform_backend.auth.dto.DadosDetalhamentoUser;
import com.thalyson.digitalcourses.course_platform_backend.category.dto.DadosDetalhamentoCategory;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de um curso")
public record DadosDetalhamentoCourse(
        @Schema(description = "ID único do curso", example = "1")
        Long id,
        @Schema(description = "Título do curso", example = "Introdução à Programação")
        String title,
        @Schema(description = "Descrição detalhada do curso", example = "Um curso introdutório para novos programadores.")
        String description,
        @Schema(description = "URL da imagem de miniatura do curso", example = "https://example.com/images/course.png")
        String thumbnailUrl,
        @Schema(description = "Indica se o curso está publicado", example = "true")
        Boolean isPublished,
        @Schema(description = "Dados do instrutor do curso")
        DadosDetalhamentoUser instructor,
        @Schema(description = "Dados da categoria do curso")
        DadosDetalhamentoCategory category,
        @Schema(description = "Data e hora de criação do curso", example = "2025-08-06T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização do curso", example = "2025-08-06T11:30:00")
        LocalDateTime updatedAt) {

    public DadosDetalhamentoCourse(CourseJPA course){
        this(course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPublished(),
                (course.getInstructor() != null ? new DadosDetalhamentoUser(course.getInstructor()) : null),
                (course.getCategory() != null ? new DadosDetalhamentoCategory(course.getCategory()) : null),
                course.getCreatedAt(),
                course.getUpdatedAt());
    }
}