package com.thalyson.digitalcourses.course_platform_backend.enrollment.dto;

import com.thalyson.digitalcourses.course_platform_backend.auth.dto.DadosDetalhamentoUser;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosDetalhamentoCourse;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.jpa.EnrollmentJPA;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosDetalhamentoLesson;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Detalhes de uma matrícula de usuário em um curso")
public record DadosDetalhamentoEnrollment(
        @Schema(description = "ID único da matrícula", example = "1")
        Long id,
        @Schema(description = "Dados do usuário matriculado")
        DadosDetalhamentoUser user,
        @Schema(description = "Dados do curso da matrícula")
        DadosDetalhamentoCourse course,
        @Schema(description = "Data da matrícula", example = "2025-08-06T10:00:00")
        LocalDateTime enrollmentDate,
        @Schema(description = "Status de conclusão do curso (ex: 'IN_PROGRESS', 'COMPLETED')", example = "IN_PROGRESS")
        String completionStatus,
        @Schema(description = "Porcentagem de progresso do usuário no curso", example = "50.5")
        BigDecimal progressPercentage,
        @Schema(description = "Conjunto de IDs das aulas que já foram concluídas pelo usuário", example = "[1, 2, 5]")
        Set<Long> completedLessonsIds,
        @Schema(description = "Dados da última aula visualizada pelo usuário")
        DadosDetalhamentoLesson lastWatchedLesson,
        @Schema(description = "Data e hora de criação da matrícula", example = "2025-08-06T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização da matrícula", example = "2025-08-06T11:30:00")
        LocalDateTime updatedAt) {

    public DadosDetalhamentoEnrollment(EnrollmentJPA enrollment){
        this(
                enrollment.getId(),
                (enrollment.getUser() != null ? new DadosDetalhamentoUser(enrollment.getUser()) : null),
                (enrollment.getCourse() != null ? new DadosDetalhamentoCourse(enrollment.getCourse()) : null),
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionStatus(),
                enrollment.getProgressPercentage(),
                enrollment.getCompletedLessonIds(),
                (enrollment.getLastWatchedLesson() != null ? new DadosDetalhamentoLesson(enrollment.getLastWatchedLesson()) : null),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt()
        );
    }
}