package com.thalyson.digitalcourses.course_platform_backend.controller;

import com.thalyson.digitalcourses.course_platform_backend.dto.DadosRegistroUsuario;
import com.thalyson.digitalcourses.course_platform_backend.service.TokenService;
import com.thalyson.digitalcourses.course_platform_backend.model.RoleJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.RoleRepository;
import com.thalyson.digitalcourses.course_platform_backend.service.UserService;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping
@Tag(name = "Registro de Usuário", description = "Endpoints para registrar novos usuários com diferentes perfis")
public class RegistrationController {
    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/register/student")
    @Operation(summary = "Registra um novo estudante", description = "Cria um novo usuário com o perfil de 'STUDENT'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estudante registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosUsuarioDetalhes.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (email já em uso, etc.)"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor (role não encontrada)")
    })
    public ResponseEntity registerStudent(@RequestBody @Valid DadosRegistroUsuario dados, UriComponentsBuilder uriBuilder){
        String hashedPassword = passwordEncoder.encode(dados.password());

        Optional<RoleJPA> studentRoleOptional = roleRepository.findByName("ROLE_STUDENT");
        if (studentRoleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        Set<RoleJPA> roles = Collections.singleton(studentRoleOptional.get());

        UserJPA newUser = userService.registerNewUser(dados.email(), hashedPassword, dados.firstName(),
                dados.lastName(), roles);

        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(newUser.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosUsuarioDetalhes(newUser));
    }

    @PostMapping("/register/instructor")
    @Operation(summary = "Registra um novo instrutor", description = "Cria um novo usuário com o perfil de 'INSTRUCTOR'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Instrutor registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosUsuarioDetalhes.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (email já em uso, etc.)"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor (role não encontrada)")
    })
    public ResponseEntity registerInstructor(@RequestBody @Valid DadosRegistroUsuario dados, UriComponentsBuilder uriBuilder) {
        String hashedPassword = passwordEncoder.encode(dados.password());

        Optional<RoleJPA> instructorRoleOptional = roleRepository.findByName("ROLE_INSTRUCTOR");
        if (instructorRoleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Role 'ROLE_INSTRUCTOR' não encontrada.");
        }
        Set<RoleJPA> roles = Collections.singleton(instructorRoleOptional.get());

        UserJPA newUser = userService.registerNewUser(dados.email(), hashedPassword, dados.firstName(), dados.lastName(), roles);

        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(newUser.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosUsuarioDetalhes(newUser));
    }

    public record DadosUsuarioDetalhes(Long id, String email, String firstName, String lastName){
        public DadosUsuarioDetalhes(UserJPA user){
            this(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName());
        }
    }
}
