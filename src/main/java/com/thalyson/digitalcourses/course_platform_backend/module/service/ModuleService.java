package com.thalyson.digitalcourses.course_platform_backend.module.service;

import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.exception.DuplicateResourceException;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.module.dto.DadosAtualizacaoModule;
import com.thalyson.digitalcourses.course_platform_backend.module.dto.DadosCadastroModule;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    public ModuleJPA create(CourseJPA course, DadosCadastroModule dados) {
        if (moduleRepository.findByCourseAndTitle(course, dados.title()).isPresent()){
            throw new DuplicateResourceException("Já existe um módulo com o título '" + dados.title() + "' para o curso com ID " + course.getId());
        }

        ModuleJPA newModule = new ModuleJPA(
                course,
                dados.title(),
                dados.description(),
                dados.orderIndex()
        );

        return moduleRepository.save(newModule);
    }

    public Optional<ModuleJPA> findById(Long id){
        return moduleRepository.findById(id);
    }

    public List<ModuleJPA> listModulesByCourse(CourseJPA course) {
        return moduleRepository.findByCourse(course);
    }

    @Transactional
    public void deleteModule(Long courseId, Long moduleId, UserJPA loggedInUser) {
        ModuleJPA module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado com ID: " + moduleId));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Recurso não encontrado em /courses/" + courseId + "/modules/" + moduleId);
        }

        boolean isAdmin = loggedInUser.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        boolean isCourseOwner = module.getCourse().getInstructor().getId().equals(loggedInUser.getId());

        if (!isAdmin && !isCourseOwner) {
            throw new AccessDeniedException("Usuário não tem permissão para deletar este módulo.");
        }

        moduleRepository.delete(module);
    }

    public boolean isInstructorOfModule(Long moduleId, Long userId) {
        return moduleRepository.findById(moduleId)
                .map(module -> module.getCourse().getInstructor().getId().equals(userId))
                .orElse(false);
    }

    @Transactional
    public ModuleJPA updateModule(Long courseId, Long moduleId, DadosAtualizacaoModule dados, UserJPA loggedInUser) {
        ModuleJPA module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado com ID: " + moduleId));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Recurso não encontrado em /courses/" + courseId + "/modules/" + moduleId);
        }

        boolean isAdmin = loggedInUser.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        boolean isCourseOwner = module.getCourse().getInstructor().getId().equals(loggedInUser.getId());

        if (!isAdmin && !isCourseOwner) {
            throw new AccessDeniedException("Usuário não tem permissão para atualizar este módulo.");
        }

        module.updateFields(dados);
        return moduleRepository.save(module);
    }

}