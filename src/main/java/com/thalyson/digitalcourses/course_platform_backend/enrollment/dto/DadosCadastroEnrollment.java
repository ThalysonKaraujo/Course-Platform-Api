package com.thalyson.digitalcourses.course_platform_backend.enrollment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para a criação de uma nova matrícula de usuário em um curso")
public record DadosCadastroEnrollment(
        @Schema(description = "ID do usuário a ser matriculado. Deve corresponder ao ID do usuário autenticado.", example = "1")
                @NotNull
        Long userId,
        @Schema(description = "ID do curso no qual o usuário será matriculado.", example = "1")
                @NotNull
        Long courseId) {
}