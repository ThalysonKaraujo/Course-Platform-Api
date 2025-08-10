package com.thalyson.digitalcourses.course_platform_backend.enrollment.controller;

import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.dto.DadosAtualizacaoEnrollment;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.dto.DadosCadastroEnrollment;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.dto.DadosDetalhamentoEnrollment;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.jpa.EnrollmentJPA;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.service.EnrollmentService;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enrollments")
@Tag(name = "Matrículas", description = "Endpoints para gerenciar matrículas de usuários em cursos")
@SecurityRequirement(name = "bearer-key")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma nova matrícula", description = "Permite que um usuário autenticado se matricule em um curso. O usuário só pode matricular a si mesmo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Matrícula realizada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoEnrollment.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tentativa de matricular outro usuário"),
            @ApiResponse(responseCode = "404", description = "Usuário ou curso não encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuário já matriculado neste curso")
    })
    public ResponseEntity<DadosDetalhamentoEnrollment> createEnrollment(
            @RequestBody @Valid DadosCadastroEnrollment dados,
            UriComponentsBuilder uriBuilder
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserJPA authenticatedUser = (UserJPA) authentication.getPrincipal();

        if (!authenticatedUser.getId().equals(dados.userId())) {
            throw new IllegalArgumentException("Você só pode matricular a si mesmo. O ID do usuário na requisição deve corresponder ao seu ID de usuário autenticado.");
        }

        EnrollmentJPA newEnrollment = enrollmentService.create(dados);
        URI uri = uriBuilder.path("/enrollments/{id}").buildAndExpand(newEnrollment.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoEnrollment(newEnrollment));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista as matrículas de um usuário", description = "Retorna todas as matrículas de um usuário específico. Acessível publicamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de matrículas retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<List<DadosDetalhamentoEnrollment>> listEnrollmentByStudent(
            @Parameter(description = "ID do usuário para buscar as matrículas", required = true)
            @PathVariable Long userId
    ) {
        UserJPA user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        List<EnrollmentJPA> enrollments = enrollmentService.findByUser(user);
        List<DadosDetalhamentoEnrollment> response = enrollments.stream()
                .map(DadosDetalhamentoEnrollment::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista os alunos matriculados em um curso", description = "Retorna todas as matrículas de um curso específico. Acessível publicamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de matrículas retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    public ResponseEntity<List<DadosDetalhamentoEnrollment>> listEnrollmentsByCourse(
            @Parameter(description = "ID do curso para buscar as matrículas", required = true)
            @PathVariable Long courseId
    ) {
        CourseJPA course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com ID: " + courseId));

        List<EnrollmentJPA> enrollments = enrollmentService.findByCourse(course);
        List<DadosDetalhamentoEnrollment> response = enrollments.stream()
                .map(DadosDetalhamentoEnrollment::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca uma matrícula por ID", description = "Retorna os detalhes de uma matrícula específica. Apenas o dono da matrícula ou um administrador pode acessar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matrícula encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoEnrollment.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Matrícula não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoEnrollment> getEnrollmentById(
            @Parameter(description = "ID da matrícula a ser buscada", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserJPA loggedInUser
    ) {
        EnrollmentJPA enrollment = enrollmentService.findById(id, loggedInUser);


        return ResponseEntity.ok(new DadosDetalhamentoEnrollment(enrollment));
    }

    @PutMapping("/{enrollmentId}/progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza o progresso de uma matrícula", description = "Permite registrar o progresso de um aluno em uma aula. Apenas o dono da matrícula ou um administrador pode realizar a ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progresso atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoEnrollment.class))),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos (ex: aula não pertence ao curso)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Matrícula não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoEnrollment> updateEnrollment(
            @Parameter(description = "ID da matrícula a ser atualizada", required = true)
            @PathVariable Long enrollmentId,
            @RequestBody @Valid DadosAtualizacaoEnrollment dados,
            @AuthenticationPrincipal UserJPA loggedInUser
    ) {
        EnrollmentJPA updatedEnrollment = enrollmentService.updateEnrollment(enrollmentId, dados, loggedInUser);
        return ResponseEntity.ok(new DadosDetalhamentoEnrollment(updatedEnrollment));
    }

    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancela uma matrícula", description = "Remove a matrícula de um usuário em um curso. Apenas o dono da matrícula ou um administrador pode realizar a ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Matrícula cancelada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Matrícula não encontrada")
    })
    public ResponseEntity<Void> unenroll(
            @Parameter(description = "ID da matrícula a ser cancelada", required = true)
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserJPA loggedInUser
    ){
        enrollmentService.unenroll(enrollmentId, loggedInUser);
        return ResponseEntity.noContent().build();
    }
}