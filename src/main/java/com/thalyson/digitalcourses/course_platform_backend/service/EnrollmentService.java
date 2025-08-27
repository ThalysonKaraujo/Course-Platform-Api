package com.thalyson.digitalcourses.course_platform_backend.service;

import com.thalyson.digitalcourses.course_platform_backend.model.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosAtualizacaoEnrollment;
import com.thalyson.digitalcourses.course_platform_backend.dto.DadosCadastroEnrollment;
import com.thalyson.digitalcourses.course_platform_backend.model.EnrollmentJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.EnrollmentRepository;
import com.thalyson.digitalcourses.course_platform_backend.exception.DuplicateResourceException;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import com.thalyson.digitalcourses.course_platform_backend.model.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.LessonRepository;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Transactional
    public EnrollmentJPA create(DadosCadastroEnrollment dados){
        UserJPA user = userRepository.findById(dados.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dados.userId()));

        CourseJPA course = courseRepository.findById(dados.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com ID: " + dados.courseId()));

        Optional<EnrollmentJPA> existingEnrollment = enrollmentRepository.findByUserAndCourse(user, course);
        if (existingEnrollment.isPresent()){
            throw new DuplicateResourceException("O usuário com ID " + dados.userId() + " já está matriculado no curso com ID " + dados.courseId() + ".");
        }

        EnrollmentJPA newEnrollment = new EnrollmentJPA(user, course);

        return enrollmentRepository.save(newEnrollment);
    }

    public List<EnrollmentJPA> findAll(){
        return enrollmentRepository.findAll();
    }

    public EnrollmentJPA findById(Long id, UserJPA loggedInUser) {
        EnrollmentJPA enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matrícula não encontrada com ID: " + id));


        checkOwnership(enrollment, loggedInUser);

        return enrollment;
    }


    public List<EnrollmentJPA> findByCourse(CourseJPA course){
        return enrollmentRepository.findByCourse(course);
    }

    public List<EnrollmentJPA> findByUser(UserJPA user){
        return enrollmentRepository.findByUser(user);
    }


    @Transactional
    public EnrollmentJPA updateEnrollment(Long enrollmentId, DadosAtualizacaoEnrollment dados, UserJPA loggedInUser) {
        EnrollmentJPA enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Matrícula não encontrada com ID: " + enrollmentId));

        checkOwnership(enrollment, loggedInUser);

        LessonJPA lesson = lessonRepository.findById(dados.lessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Aula não encontrada com ID: " + dados.lessonId()));

        if (!lesson.getModule().getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw new IllegalArgumentException("A aula não pertence ao curso da matrícula.");
        }

        enrollment.markLessonAsCompleted(lesson.getId());
        updateProgress(enrollment);
        enrollment.setLastWatchedLesson(lesson);

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenroll(Long enrollmentId, UserJPA loggedInUser) {
        EnrollmentJPA enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Matrícula não encontrada com ID: " + enrollmentId));

        checkOwnership(enrollment, loggedInUser);

        enrollmentRepository.delete(enrollment);
    }

    private void checkOwnership(EnrollmentJPA enrollment, UserJPA user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = enrollment.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Usuário não tem permissão para acessar este recurso.");
        }
    }


    private void updateProgress(EnrollmentJPA enrollment) {
        Long totalLessonsInCourse = lessonRepository.countByCourseId(enrollment.getCourse().getId());
        if (totalLessonsInCourse == 0) {
            enrollment.setProgressPercentage(BigDecimal.ZERO);
            return;
        }

        BigDecimal progress = BigDecimal.valueOf(enrollment.getCompletedLessonIds().size())
                .divide(BigDecimal.valueOf(totalLessonsInCourse), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        enrollment.setProgressPercentage(progress);

        if (progress.compareTo(BigDecimal.valueOf(100)) == 0) {
            enrollment.setCompletionStatus("COMPLETED");
        } else {
            enrollment.setCompletionStatus("IN_PROGRESS");
        }
    }

    public Optional<EnrollmentJPA> findById(Long id) {
        return enrollmentRepository.findById(id);
    }
}