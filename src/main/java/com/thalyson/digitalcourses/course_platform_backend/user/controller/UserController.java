package com.thalyson.digitalcourses.course_platform_backend.user.controller;

import com.thalyson.digitalcourses.course_platform_backend.auth.controller.LoginController;
import com.thalyson.digitalcourses.course_platform_backend.auth.service.TokenService;
import com.thalyson.digitalcourses.course_platform_backend.user.dto.DadosAtualizacaoUser;
import com.thalyson.digitalcourses.course_platform_backend.user.dto.DadosDetalhamentoUser;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento do perfil do usuário autenticado")
@SecurityRequirement(name = "bearer-key")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém o perfil do usuário autenticado", description = "Retorna os detalhes do perfil do usuário que está logado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes do perfil retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoUser.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<DadosDetalhamentoUser> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserJPA authenticatedUser = (UserJPA) authentication.getPrincipal();
        return ResponseEntity.ok(new DadosDetalhamentoUser(authenticatedUser));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza o perfil do usuário autenticado", description = "Permite a atualização de dados do perfil do usuário logado, retornando um novo token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LoginController.DadosTokenJWT.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<LoginController.DadosTokenJWT> updateMyProfile(@RequestBody @Valid DadosAtualizacaoUser dados){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserJPA authenticatedUser = (UserJPA) authentication.getPrincipal();
        UserJPA updatedUser = userService.update(authenticatedUser.getId(), dados);
        var newToken = tokenService.gerarToken(updatedUser);
        return ResponseEntity.ok(new LoginController.DadosTokenJWT(newToken));
    }
}
