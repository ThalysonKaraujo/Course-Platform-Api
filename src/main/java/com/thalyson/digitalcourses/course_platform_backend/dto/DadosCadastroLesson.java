package com.thalyson.digitalcourses.course_platform_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Schema(description = "Dados para o cadastro de uma nova aula em um módulo")
public record DadosCadastroLesson(
        @NotBlank
        @Schema(description = "Título da aula", example = "Estruturas de Dados em Java", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotBlank
        @Schema(description = "Descrição detalhada da aula", example = "Nesta aula, exploraremos as principais estruturas de dados da linguagem Java.", requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @NotBlank
        @Schema(description = "URL do vídeo da aula no YouTube", example = "https://www.youtube.com/watch?v=xxxxxxxxxxx", requiredMode = Schema.RequiredMode.REQUIRED)
        String youtubeVideoUrl,
        @Schema(description = "Duração da aula em segundos", example = "3600")
        Integer durationSeconds,
        @NotNull
        @Schema(description = "Índice de ordenação da aula dentro do módulo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer orderIndex) {
}