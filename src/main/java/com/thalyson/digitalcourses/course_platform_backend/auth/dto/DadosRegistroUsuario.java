package com.thalyson.digitalcourses.course_platform_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registro de um novo usuário")
public record DadosRegistroUsuario(
        @NotBlank(message = "{email.obrigatorio}")
        @Email (message = "{email.invalido}")
        @Schema(description = "Endereço de e-mail do usuário (único)", example = "novo.usuario@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "{senha.obrigatorio}")
        @Size(min = 6, max = 30, message = "{senha.tamanho}")
        @Schema(description = "Senha do usuário (mínimo 6 e máximo 30 caracteres)", example = "SenhaSegura123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @NotBlank(message = "{primeiro.nome.obrigatorio}")
        @Schema(description = "Primeiro nome do usuário", example = "Maria", requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "{ultimo.nome.obrigatorio}" )
        @Schema(description = "Sobrenome do usuário", example = "Fernandes", requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName
) {}