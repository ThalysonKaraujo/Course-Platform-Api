package com.thalyson.digitalcourses.course_platform_backend.lesson.controller;


import com.thalyson.digitalcourses.course_platform_backend.enrollment.validation.annotation.ExistingLessonInCourse;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosAtualizacaoLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosCadastroLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosDetalhamentoLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.lesson.service.LessonService;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.service.ModuleService;
import com.thalyson.digitalcourses.course_platform_backend.module.validation.annotation.ExistingModule;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/modules/{moduleId}/lessons")
@Validated
@Tag(name = "Aulas", description = "Endpoints para gerenciamento de aulas dentro de um módulo")
@SecurityRequirement(name = "bearer-key")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private ModuleService moduleService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma nova aula em um módulo", description = "Permite que um instrutor ou administrador adicione uma nova aula a um módulo existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Aula criada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoLesson.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<DadosDetalhamentoLesson> createLesson(
            @Parameter(description = "ID do módulo ao qual a aula será adicionada", required = true)
            @PathVariable Long moduleId,
            @RequestBody @Valid DadosCadastroLesson dados,
            @AuthenticationPrincipal UserJPA loggedInUser,
            UriComponentsBuilder uriBuilder
    ){

        LessonJPA newLesson = lessonService.create(moduleId, dados, loggedInUser);

        URI uri = uriBuilder.path("/modules/{moduleId}/lessons/{lessonId}")
                .buildAndExpand(moduleId, newLesson.getId())
                .toUri();

        return ResponseEntity.created(uri).body(new DadosDetalhamentoLesson(newLesson));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista todas as aulas de um módulo", description = "Retorna a lista de aulas de um módulo específico. Visível para todos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de aulas retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado")
    })
    public ResponseEntity<List<DadosDetalhamentoLesson>> listLessonsByModule(
            @Parameter(description = "ID do módulo para listar as aulas", required = true)
            @PathVariable Long moduleId
    ) {
        ModuleJPA module = moduleService.findById(moduleId).get();
        List<LessonJPA> lessons = lessonService.listByModule(module);
        List<DadosDetalhamentoLesson> dadosDetalhamento = lessons.stream().map(DadosDetalhamentoLesson::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dadosDetalhamento);
    }

    @GetMapping("/{lessonId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca uma aula por ID", description = "Retorna os detalhes de uma aula específica de um módulo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aula encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Aula ou módulo não encontrado"),
            @ApiResponse(responseCode = "400", description = "O ID da aula não pertence ao módulo fornecido")
    })
    public ResponseEntity<DadosDetalhamentoLesson> getLessonById(
            @Parameter(description = "ID do módulo", required = true)
            @PathVariable @ExistingModule Long moduleId,
            @Parameter(description = "ID da aula a ser buscada", required = true)
            @PathVariable Long lessonId
    ) {
        LessonJPA lesson = lessonService.findById(lessonId)
                .orElseThrow(() -> new NoSuchElementException("Aula não encontrada com ID: " + lessonId));

        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new ResourceNotFoundException("Aula com ID " + lessonId + " não pertence ao módulo com ID " + moduleId + ".");
        }
        return ResponseEntity.ok(new DadosDetalhamentoLesson(lesson));
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza uma aula", description = "Atualiza os dados de uma aula. Apenas o instrutor do curso ou um administrador pode realizar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Aula atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoLesson.class))),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos ou a aula não pertence ao módulo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Aula ou módulo não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoLesson> updateLesson(
            @Parameter(description = "ID do módulo", required = true)
            @PathVariable Long moduleId,
            @Parameter(description = "ID da aula a ser atualizada", required = true)
            @PathVariable Long lessonId,
            @RequestBody @Valid DadosAtualizacaoLesson dados,
            @AuthenticationPrincipal UserJPA loggedInUser
            ) {

        LessonJPA updatedLesson = lessonService.update(moduleId, lessonId, dados, loggedInUser);
        return ResponseEntity.ok(new DadosDetalhamentoLesson(updatedLesson));
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Exclui uma aula", description = "Exclui uma aula. Apenas o instrutor do curso ou um administrador pode realizar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Aula excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "A aula não pertence ao módulo fornecido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Aula ou módulo não encontrado")
    })
    public ResponseEntity<Void> deleteLesson(
            @Parameter(description = "ID do módulo", required = true)
            @PathVariable Long moduleId,
            @Parameter(description = "ID da aula a ser excluída", required = true)
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserJPA loggedInUser
    ) {
        LessonJPA lesson = lessonService.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada com ID: " + lessonId));
        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new IllegalArgumentException("Aula com ID " + lessonId + " não pertence ao módulo com ID " + moduleId + ".");
        }
        lessonService.delete(moduleId, lessonId, loggedInUser);
        return ResponseEntity.noContent().build();
    }
}
