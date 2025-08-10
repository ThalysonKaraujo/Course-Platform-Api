package com.thalyson.digitalcourses.course_platform_backend.course.dto;

import com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation.ExistingCategory;
import com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation.ExistingIntructor;
import com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation.UniqueCourseTitle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para o cadastro de um novo curso")
public record DadosCadastroCourse(
        @UniqueCourseTitle
        @Schema(description = "Título do curso. Deve ser único.", example = "Introdução à Programação com Java")
                @NotBlank
        String title,
        @Schema(description = "Descrição detalhada do curso", example = "Um curso completo sobre os fundamentos da programação em Java.")
                @NotBlank
        String description,
        @Schema(description = "URL da imagem de miniatura do curso", example = "https://example.com/images/java-course.png")
        String thumbnailUrl,
        @Schema(description = "Indica se o curso está publicado e visível para os estudantes", example = "true")
        Boolean isPublished,
        @NotNull(message = "O ID do instrutor é obrigatório.")
        @ExistingIntructor
        @Schema(description = "ID do instrutor responsável pelo curso", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long instructorId,
        @NotNull(message = "O ID da categoria é obrigatório.")
        @ExistingCategory
        @Schema(description = "ID da categoria à qual o curso pertence", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer categoryId){}