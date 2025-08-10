package com.thalyson.digitalcourses.course_platform_backend.lesson.dto;

import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.dto.DadosDetalhamentoModule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de uma aula")
public record DadosDetalhamentoLesson(
        @Schema(description = "ID único da aula", example = "1")
        Long id,
        @Schema(description = "Título da aula", example = "Introdução aos Arrays")
        String title,
        @Schema(description = "Descrição detalhada da aula", example = "Nesta aula, você aprenderá sobre arrays.")
        String description,
        @Schema(description = "URL do vídeo da aula no YouTube", example = "https://www.youtube.com/watch?v=xxxxxxxxxxx")
        String youtubeVideoUrl,
        @Schema(description = "Duração da aula em segundos", example = "1200")
        Integer durationSeconds,
        @Schema(description = "Índice de ordenação da aula dentro do módulo", example = "1")
        Integer orderIndex,
        @Schema(description = "Dados do módulo ao qual a aula pertence")
        DadosDetalhamentoModule module,
        @Schema(description = "Data e hora de criação da aula", example = "2025-08-06T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização da aula", example = "2025-08-06T11:30:00")
        LocalDateTime updatedAt) {

    public DadosDetalhamentoLesson(LessonJPA lesson){
        this(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getYoutubeVideoUrl(),
                lesson.getDurationSeconds(),
                lesson.getOrderIndex(),
                (lesson.getModule() != null ? new DadosDetalhamentoModule(lesson.getModule()) : null),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}