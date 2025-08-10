package com.thalyson.digitalcourses.course_platform_backend.enrollment.dto;

import com.thalyson.digitalcourses.course_platform_backend.enrollment.validation.annotation.ExistingLessonInCourse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados para a atualização de uma matrícula, tipicamente para registrar o progresso em uma aula.")
public record DadosAtualizacaoEnrollment(
        @NotNull(message = "O ID da aula é obrigatório.")
        @Schema(description = "ID da aula que foi concluída. Deve existir no curso matriculado.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long lessonId
) {
}