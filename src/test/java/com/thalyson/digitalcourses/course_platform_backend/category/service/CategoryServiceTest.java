package com.thalyson.digitalcourses.course_platform_backend.category.service;

import com.thalyson.digitalcourses.course_platform_backend.category.dto.DadosCadastroCategory;
import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.exception.DuplicateResourceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Deve criar uma nova categoria quando o nome for único")
    void deveCriarCategoriaComNomeUnico() {
        DadosCadastroCategory dados = new DadosCadastroCategory("Tecnologia");

        CategoryJPA categoriaSalva = new CategoryJPA();
        categoriaSalva.setName("Tecnologia");

        when(categoryRepository.findByName(dados.name())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryJPA.class))).thenReturn(categoriaSalva);

        CategoryJPA resultado = categoryService.create(dados);

        verify(categoryRepository, times(1)).save(any(CategoryJPA.class));

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals("Tecnologia", resultado.getName());
    }

    @Test
    @DisplayName("Não deve criar uma categoria se o nome já existir")
    void naoDeveCriarCategoriaComNomeExistente() {
        DadosCadastroCategory dados = new DadosCadastroCategory("Tecnologia");

        CategoryJPA categoriaExistente = new CategoryJPA();
        categoriaExistente.setName("Tecnologia");


        when(categoryRepository.findByName(dados.name())).thenReturn(Optional.of(categoriaExistente));


        Assertions.assertThrows(DuplicateResourceException.class, () -> {
            categoryService.create(dados);
        });

        verify(categoryRepository, never()).save(any(CategoryJPA.class));
    }
}