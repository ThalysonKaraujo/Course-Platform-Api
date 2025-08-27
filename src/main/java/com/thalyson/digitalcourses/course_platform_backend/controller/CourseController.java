package com.thalyson.digitalcourses.course_platform_backend.controller;

import com.thalyson.digitalcourses.course_platform_backend.dto.DadosAtualizacaoCourse;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosCadastroCourse;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosDetalhamentoCourse;
import com.thalyson.digitalcourses.course_platform_backend.model.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.service.CourseService;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/courses")
@Tag(name = "Cursos", description = "Endpoints para gerenciamento de cursos")
@SecurityRequirement(name = "bearer-key")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Cria um novo curso", description = "Permite que um instrutor ou administrador crie um novo curso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Curso criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoCourse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Dados de entrada inválidos\"}"))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content)
    })
    public ResponseEntity<DadosDetalhamentoCourse> createCourse(@RequestBody @Valid DadosCadastroCourse dados, UriComponentsBuilder uriComponentsBuilder) {
        CourseJPA newCourse = courseService.create(dados);
        URI uri = uriComponentsBuilder.path("/courses/{id}").buildAndExpand(newCourse.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoCourse(newCourse));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista todos os cursos", description = "Retorna uma lista paginada de todos os cursos, visível para todos os usuários.")
    @ApiResponse(responseCode = "200", description = "Lista de cursos retornada com sucesso")
    public ResponseEntity<Page<DadosDetalhamentoCourse>> listAllCourses(
            @Parameter(description = "Configurações de paginação") @PageableDefault(size = 10, sort = {"title"}) Pageable pageable
    ) {
        Page<CourseJPA> coursesPage = courseService.findAll(pageable);
        Page<DadosDetalhamentoCourse> dadosDetalhamentoPage = coursesPage.map(DadosDetalhamentoCourse::new);
        return ResponseEntity.ok(dadosDetalhamentoPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca um curso por ID", description = "Retorna os detalhes de um curso específico com base no seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Curso encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoCourse.class))),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Curso não encontrado com ID: 1\"}")))
    })
    public ResponseEntity<DadosDetalhamentoCourse> getCourseById(
            @Parameter(description = "ID do curso a ser buscado", required = true)
            @PathVariable Long id
    ) {
        CourseJPA course = courseService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Curso não encontrado com ID: " + id));
        return ResponseEntity.ok(new DadosDetalhamentoCourse(course));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @courseService.isInstructorOfCourse(#id, authentication.principal)")
    @Operation(summary = "Atualiza um curso", description = "Atualiza os dados de um curso. Apenas o instrutor do curso ou um administrador pode realizar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Curso atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoCourse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoCourse> updateCourse(
            @Parameter(description = "ID do curso a ser atualizado", required = true)
            @PathVariable Long id,
            @RequestBody @Valid DadosAtualizacaoCourse dados
            ) {
        CourseJPA updatedCourse = courseService.update(id, dados);
        return ResponseEntity.ok(new DadosDetalhamentoCourse(updatedCourse));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um curso", description = "Exclui um curso. Apenas o instrutor do curso ou um administrador pode realizar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Curso excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "ID do curso a ser excluído", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserJPA authenticatedUser
    ) {
        courseService.delete(id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
