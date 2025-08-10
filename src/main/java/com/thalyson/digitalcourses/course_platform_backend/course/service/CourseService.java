package com.thalyson.digitalcourses.course_platform_backend.course.service;

import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosAtualizacaoCourse;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosCadastroCourse;
import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.exception.AccessDeniedByBusinessException;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public CourseJPA create(DadosCadastroCourse dados){
        UserJPA instructor = userRepository.findById(dados.instructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Instrutor não encontrado com ID: " + dados.instructorId()));

        CategoryJPA category = categoryRepository.findById(dados.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + dados.categoryId()));


        CourseJPA newCourse = new CourseJPA(
                dados.title(),
                dados.description(),
                dados.thumbnailUrl(),
                dados.isPublished(),
                instructor,
                category
        );
        return courseRepository.save(newCourse);
    }

    @Transactional
    public CourseJPA update(Long id, DadosAtualizacaoCourse dados){
        CourseJPA course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com ID: " + id));

        if (dados.instructorId() != null) {
            UserJPA instructor = userRepository.findById(dados.instructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instrutor não encontrado com ID: " + dados.instructorId()));
            course.setInstructor(instructor);
        }

        if (dados.categoryId() != null){
            CategoryJPA category = categoryRepository.findById(dados.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + dados.categoryId()));
            course.setCategory(category);
        }

        course.updateFields(dados);
        return courseRepository.save(course);
    }

    @Transactional
    public void delete(Long id, UserJPA authenticatedUser) {
        CourseJPA course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com Id: " + id));

        boolean isAdmin = authenticatedUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        boolean isInstructorOfCourse = course.getInstructor().getId().equals(authenticatedUser.getId());

        if (!isAdmin && !isInstructorOfCourse) {
            throw new AccessDeniedByBusinessException("Usuário não tem permissão para excluir este curso.");
        }

        courseRepository.delete(course);
    }

    public Optional<CourseJPA> findById(Long id){
        return courseRepository.findById(id);
    }

    public Page<CourseJPA> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public boolean isInstructorOfCourse(Long courseId, UserJPA authenticatedUser) {
        return courseRepository.findById(courseId)
                .map(course -> course.getInstructor().getId().equals(authenticatedUser.getId()))
                .orElse(false);
    }

}
