package com.thalyson.digitalcourses.course_platform_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para a atualização do perfil de um usuário")
public record DadosAtualizacaoUser(
        @Email
        @Schema(description = "Novo endereço de e-mail do usuário", example = "novo.email@example.com")
        String email,
        @Size(max = 100, message = "O primeiro nome não pode exceder {max} caracteres.")
        @Schema(description = "Novo primeiro nome do usuário (máximo 100 caracteres)", example = "Ana")
        String firstName,
        @Size(max = 100, message = "O último nome não pode exceder {max} caracteres.")
        @Schema(description = "Novo sobrenome do usuário (máximo 100 caracteres)", example = "Pereira")
        String lastName
) {
}