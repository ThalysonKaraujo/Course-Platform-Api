package com.thalyson.digitalcourses.course_platform_backend.dto;

import com.thalyson.digitalcourses.course_platform_backend.model.ModuleJPA;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Detalhes de um módulo de curso")
public record DadosDetalhamentoModule(
        @Schema(description = "ID único do módulo", example = "1")
        Long id,
        @Schema(description = "Título do módulo", example = "Módulo 1: Java Básico")
        String title,
        @Schema(description = "Descrição detalhada do módulo", example = "Conceitos introdutórios da linguagem Java.")
        String description,
        @Schema(description = "Índice de ordenação do módulo dentro do curso", example = "1")
        Integer orderIndex,
        @Schema(description = "Dados do curso ao qual o módulo pertence")
        DadosDetalhamentoCourse course,
        @Schema(description = "Data e hora de criação do módulo", example = "2025-08-06T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização do módulo", example = "2025-08-06T11:30:00")
        LocalDateTime updatedAt) {

    public DadosDetalhamentoModule(ModuleJPA module){
        this(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getOrderIndex(),
                (module.getCourse() != null ? new DadosDetalhamentoCourse(module.getCourse()) : null),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }
}