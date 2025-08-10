package com.thalyson.digitalcourses.course_platform_backend.course.service;

import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosCadastroCourse;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
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
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    @DisplayName("Deve criar um novo curso com sucesso quando o instrutor e a categoria existirem")
    void deveCriarCursoComSucesso() {
        DadosCadastroCourse dados = new DadosCadastroCourse(
                "Título do Curso", "Descrição", "url", true, 1L, 1);

        UserJPA instrutorMock = new UserJPA();
        instrutorMock.setId(1L);
        CategoryJPA categoriaMock = new CategoryJPA();
        categoriaMock.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(instrutorMock));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(categoriaMock));

        courseService.create(dados);

        verify(courseRepository, times(1)).save(any(CourseJPA.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se o ID do instrutor não for encontrado")
    void deveLancarExcecaoSeInstrutorNaoEncontrado() {
        DadosCadastroCourse dados = new DadosCadastroCourse(
                "Título do Curso", "Descrição", "url", true, 99L, 1);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            courseService.create(dados);
        });

        verify(courseRepository, never()).save(any(CourseJPA.class));
    }

    @Test
    @DisplayName("Deve lançar exceção se o ID da categoria não for encontrado")
    void deveLancarExcecaoSeCategoriaNaoEncontrada() {
        DadosCadastroCourse dados = new DadosCadastroCourse(
                "Título do Curso", "Descrição", "url", true, 1L, 99);

        UserJPA instrutorMock = new UserJPA();
        instrutorMock.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(instrutorMock));
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            courseService.create(dados);
        });

        verify(courseRepository, never()).save(any(CourseJPA.class));
    }
}