package com.thalyson.digitalcourses.course_platform_backend.controller;


import com.thalyson.digitalcourses.course_platform_backend.model.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.service.CourseService;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosAtualizacaoModule;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosCadastroModule;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosDetalhamentoModule;
import com.thalyson.digitalcourses.course_platform_backend.model.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.service.ModuleService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/courses/{courseId}/modules")
@Tag(name = "Módulos", description = "Endpoints para gerenciamento de módulos em um curso")
@SecurityRequirement(name = "bearer-key")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Cria um novo módulo em um curso", description = "Permite que um instrutor ou administrador adicione um novo módulo a um curso existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Módulo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoModule.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<DadosDetalhamentoModule> createModule(
            @Parameter(description = "ID do curso ao qual o módulo será adicionado", required = true)
            @PathVariable Long courseId,
            @RequestBody @Valid DadosCadastroModule dados,
            UriComponentsBuilder uriBuilder
            ){

        System.out.println("DEBUG - ModuleController: courseId recebido via PathVariable: " + courseId);

        CourseJPA course = courseService.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com ID: " + courseId));

        ModuleJPA newModule = moduleService.create(course, dados);

        URI uri = uriBuilder.path("/courses/{courseId}/modules/{moduleId}")
                .buildAndExpand(courseId, newModule.getId())
                .toUri();

        return ResponseEntity.created(uri).body(new DadosDetalhamentoModule(newModule));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista todos os módulos de um curso", description = "Retorna a lista de módulos de um curso específico. Visível para todos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de módulos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado")
    })
    public ResponseEntity<List<DadosDetalhamentoModule>> listModulesByCourse(
            @Parameter(description = "ID do curso para listar os módulos", required = true)
            @PathVariable Long courseId
    ) {
        CourseJPA course = courseService.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com ID: " + courseId));
        List<ModuleJPA> modules = moduleService.listModulesByCourse(course);
        List<DadosDetalhamentoModule> dadosDetalhamento = modules.stream().map(DadosDetalhamentoModule::new).toList();
        return ResponseEntity.ok(dadosDetalhamento);
    }

    @GetMapping("/{moduleId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca um módulo por ID", description = "Retorna os detalhes de um módulo específico de um curso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Módulo encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Módulo ou curso não encontrado"),
            @ApiResponse(responseCode = "400", description = "O ID do módulo não pertence ao curso fornecido")
    })
    public ResponseEntity<DadosDetalhamentoModule> getModuleById(
            @Parameter(description = "ID do curso", required = true)
            @PathVariable Long courseId,
            @Parameter(description = "ID do módulo a ser buscado", required = true)
            @PathVariable Long moduleId
    ) {
        ModuleJPA module = moduleService.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado com ID: " + moduleId));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Módulo com ID " + moduleId + " não pertence ao curso com ID " + courseId + ".");
        }

        return ResponseEntity.ok(new DadosDetalhamentoModule(module));
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza um módulo", description = "Atualiza os dados de um módulo. Apenas o instrutor do curso ou um administrador pode realizar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Módulo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoModule.class))),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoModule> updateModule(
            @Parameter(description = "ID do curso", required = true)
            @PathVariable Long courseId,
            @Parameter(description = "ID do módulo a ser atualizado", required = true)
            @PathVariable Long moduleId,
            @RequestBody @Valid DadosAtualizacaoModule dados,
            @AuthenticationPrincipal UserJPA loggedInUser
            ) {

        ModuleJPA updatedModule = moduleService.updateModule(courseId, moduleId, dados, loggedInUser);
        return ResponseEntity.ok(new DadosDetalhamentoModule(updatedModule));
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Exclui um módulo", description = "Exclui um módulo. Apenas o instrutor do curso ou um administrador pode realizar esta ação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Módulo excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Módulo não encontrado")
    })
    public ResponseEntity<Void> deleteModule(
            @Parameter(description = "ID do módulo a ser excluído", required = true)
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @AuthenticationPrincipal UserJPA loggedInUser
    ) {
        moduleService.deleteModule(courseId, moduleId, loggedInUser);
        return ResponseEntity.noContent().build();
    }
}
