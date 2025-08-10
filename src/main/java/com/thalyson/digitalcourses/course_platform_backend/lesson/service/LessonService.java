package com.thalyson.digitalcourses.course_platform_backend.lesson.service;

import com.thalyson.digitalcourses.course_platform_backend.exception.DuplicateResourceException;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosAtualizacaoLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosCadastroLesson;
import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.lesson.repository.LessonRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private ModuleRepository moduleRepository;

    @Transactional
    public LessonJPA create(Long moduleId, DadosCadastroLesson dados, UserJPA loggedInUser) {
        ModuleJPA module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado com ID: " + moduleId));

        checkPermission(module, loggedInUser);

        if (lessonRepository.findByModuleAndOrderIndex(module, dados.orderIndex()).isPresent()) {
            throw new DuplicateResourceException("Já existe uma aula com a ordem " + dados.orderIndex() + " neste módulo.");
        }

        LessonJPA newLesson = new LessonJPA(module, dados.title(), dados.description(), dados.youtubeVideoUrl(), dados.durationSeconds(), dados.orderIndex());
        return lessonRepository.save(newLesson);
    }

    @Transactional
    public LessonJPA update(Long moduleId, Long lessonId, DadosAtualizacaoLesson dados, UserJPA loggedInUser) {
        LessonJPA lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada com ID: " + lessonId));

        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new ResourceNotFoundException("Recurso não encontrado em /modules/" + moduleId + "/lessons/" + lessonId);
        }

        checkPermission(lesson.getModule(), loggedInUser);

        lesson.updateFields(dados);
        return lessonRepository.save(lesson);
    }

    @Transactional
    public void delete(Long moduleId, Long lessonId, UserJPA loggedInUser) {
        LessonJPA lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada com ID: " + lessonId));

        if (!lesson.getModule().getId().equals(moduleId)) {
            throw new ResourceNotFoundException("Recurso não encontrado em /modules/" + moduleId + "/lessons/" + lessonId);
        }

        checkPermission(lesson.getModule(), loggedInUser);

        lessonRepository.delete(lesson);
    }

    private void checkPermission(ModuleJPA module, UserJPA user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        boolean isCourseOwner = module.getCourse().getInstructor().getId().equals(user.getId());

        if (!isAdmin && !isCourseOwner) {
            throw new AccessDeniedException("Usuário não tem permissão para modificar aulas neste módulo.");
        }
    }

    public Optional<LessonJPA> findById(Long id) {
        return lessonRepository.findById(id);
    }

    public List<LessonJPA> listByModule(ModuleJPA module) {
        return lessonRepository.findByModule(module);
    }
}