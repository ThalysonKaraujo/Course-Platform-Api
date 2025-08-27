package com.thalyson.digitalcourses.course_platform_backend.controller;

import com.thalyson.digitalcourses.course_platform_backend.dto.DadosCadastroCategory;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosDetalhamentoCategory;
import com.thalyson.digitalcourses.course_platform_backend.model.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.service.CategoryService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@Validated
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
@SecurityRequirement(name = "bearer-key")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Cria uma nova categoria", description = "Cria uma nova categoria. Apenas administradores e instrutores podem criar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoCategory.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Conflito: Categoria com o mesmo título já existe")
    })
    public ResponseEntity<DadosDetalhamentoCategory> createCategory(@RequestBody @Valid DadosCadastroCategory dadosCadastroCategory, UriComponentsBuilder uriComponentsBuilder){
        CategoryJPA newCategory = categoryService.create(dadosCadastroCategory);
        URI uri = uriComponentsBuilder.path("/category/{id}").buildAndExpand(newCategory.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoCategory(newCategory));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista todas as categorias", description = "Retorna uma lista de todas as categorias. Acesso permitido a todos os usuários.")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    public ResponseEntity<List<DadosDetalhamentoCategory>> listAllCategories() {
        List<CategoryJPA> categories = categoryService.findAll();
        List<DadosDetalhamentoCategory> dadosDetalhamento = categories.stream()
                .map(DadosDetalhamentoCategory::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dadosDetalhamento);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Busca uma categoria por ID", description = "Retorna os detalhes de uma categoria específica. Acesso permitido a todos os usuários.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada com o ID fornecido")
    })
    public ResponseEntity<DadosDetalhamentoCategory> getCategoryById(
            @Parameter(description = "ID da categoria a ser buscada", required = true)
            @PathVariable Integer id
    ) {
        CategoryJPA category = categoryService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria não encontrada com ID: " + id));

        return ResponseEntity.ok(new DadosDetalhamentoCategory(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma categoria", description = "Atualiza os dados de uma categoria. Apenas administradores podem atualizar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = DadosDetalhamentoCategory.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada com o ID fornecido"),
            @ApiResponse(responseCode = "409", description = "Conflito: Categoria com o mesmo título já existe")
    })
    public ResponseEntity<DadosDetalhamentoCategory> updateCategory(
            @Parameter(description = "ID da categoria a ser atualizada", required = true)
            @PathVariable Integer id,
            @RequestBody @Valid DadosCadastroCategory dados
    ) {
        CategoryJPA updatedCategory = categoryService.update(id, dados);
        return ResponseEntity.ok(new DadosDetalhamentoCategory(updatedCategory));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui uma categoria", description = "Exclui uma categoria. Apenas administradores podem excluir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada com o ID fornecido")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID da categoria a ser excluída", required = true)
            @PathVariable Integer id
    ) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
