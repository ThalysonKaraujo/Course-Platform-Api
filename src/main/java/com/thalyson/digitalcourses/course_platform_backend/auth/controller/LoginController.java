package com.thalyson.digitalcourses.course_platform_backend.auth.controller;

import com.thalyson.digitalcourses.course_platform_backend.auth.dto.DadosLogin;
import com.thalyson.digitalcourses.course_platform_backend.auth.service.TokenService;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação", description = "Endpoints para login de usuários")
public class LoginController {
    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    @Operation(summary = "Realiza o login do usuário", description = "Autentica o usuário com email e senha e retorna um token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido, retorna o token JWT",
                    content = @Content(schema = @Schema(implementation = DadosTokenJWT.class))),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou dados de entrada incorretos"),
            @ApiResponse(responseCode = "403", description = "Credenciais inválidas. Não autorizado.")
    })
    public ResponseEntity<DadosTokenJWT> Login(@RequestBody @Valid DadosLogin dados){
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.password());
        var authentication = manager.authenticate(authenticationToken);
        var jwtToken = tokenService.gerarToken((UserJPA) authentication.getPrincipal());
        return ResponseEntity.ok(new DadosTokenJWT(jwtToken));
    }

    public record DadosTokenJWT(String token) {}
}
