package com.thalyson.digitalcourses.course_platform_backend.auth.dto;

import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalhes do perfil de um usuário")
public record DadosDetalhamentoUser(
        @Schema(description = "ID único do usuário", example = "1")
        Long id,
        @Schema(description = "Endereço de e-mail do usuário, usado para login", example = "usuario@example.com")
        String email,
        @Schema(description = "Primeiro nome do usuário", example = "João")
        String firstName,
        @Schema(description = "Sobrenome do usuário", example = "Silva")
        String lastName
) {
    public DadosDetalhamentoUser(UserJPA user){
        this(user.getId(),user.getEmail(), user.getFirstName(), user.getLastName());
    }
}