package com.thalyson.digitalcourses.course_platform_backend.lesson.dto;

import com.thalyson.digitalcourses.course_platform_backend.lesson.validation.annotation.UniqueLessonOrderIndexInModule;
import com.thalyson.digitalcourses.course_platform_backend.lesson.validation.annotation.UniqueLessonTitleInModule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para a atualização de uma aula existente")
public record DadosAtualizacaoLesson(
        @NotBlank(message = "O título da aula não pode ser em branco.")
        @Size(max = 255, message = "O título não pode exceder {max} caracteres.")
        @UniqueLessonTitleInModule
        @Schema(description = "Novo título da aula (máximo 255 caracteres, deve ser único no módulo)", example = "Estruturas de Dados Avançadas", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Size(max = 500, message = "A descrição não pode exceder {max} caracteres.")
        @Schema(description = "Nova descrição da aula (máximo 500 caracteres)", example = "Exploração aprofundada de árvores binárias e grafos.")
                @NotBlank
        String description,

        @NotBlank(message = "A URL do vídeo do YouTube não pode ser em branco.")
        @Size(max = 200, message = "A URL do vídeo não pode exceder {max} caracteres.")
        @Schema(description = "Nova URL do vídeo da aula no YouTube (máximo 200 caracteres)", example = "https://www.youtube.com/watch?v=new-video", requiredMode = Schema.RequiredMode.REQUIRED)
        String youtubeVideoUrl,

        @Min(value = 0, message = "A duração da aula deve ser um valor positivo.")
        @Schema(description = "Nova duração da aula em segundos (valor mínimo 0)", example = "4200")
        Integer durationSeconds,

        @Min(value = 1, message = "A ordem da aula deve ser pelo menos 1.")
        @UniqueLessonOrderIndexInModule
        @Schema(description = "Novo índice de ordenação da aula dentro do módulo (mínimo 1, deve ser único no módulo)", example = "2")
        Integer orderIndex
) {
}