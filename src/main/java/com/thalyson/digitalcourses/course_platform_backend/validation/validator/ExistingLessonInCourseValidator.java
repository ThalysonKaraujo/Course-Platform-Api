package com.thalyson.digitalcourses.course_platform_backend.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.model.EnrollmentJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.EnrollmentRepository;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingLessonInCourse;
import com.thalyson.digitalcourses.course_platform_backend.model.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.LessonRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExistingLessonInCourseValidator implements ConstraintValidator<ExistingLessonInCourse, Long> {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean isValid(Long lessonId, ConstraintValidatorContext context) {
        if (lessonId == null) {
            return true;
        }

        Pattern p = Pattern.compile("/enrollments/(\\d+)");
        Matcher m = p.matcher(request.getRequestURI());
        Long enrollmentId = null;
        if (m.find()) {
            enrollmentId = Long.parseLong(m.group(1));
        }

        if (enrollmentId == null) {
            return false;
        }

        Optional<EnrollmentJPA> enrollmentOptional = enrollmentRepository.findById(enrollmentId);
        Optional<LessonJPA> lessonOptional = lessonRepository.findById(lessonId);

        // CORRIGIDO: Verificar a presença dos objetos antes de tentar usá-los
        if (enrollmentOptional.isEmpty() || lessonOptional.isEmpty()) {
            context.disableDefaultConstraintViolation();
            if (enrollmentOptional.isEmpty()) {
                context.buildConstraintViolationWithTemplate("Matrícula não encontrada com ID: " + enrollmentId).addConstraintViolation();
            } else {
                context.buildConstraintViolationWithTemplate("Aula não encontrada com ID: " + lessonId).addConstraintViolation();
            }
            return false;
        }

        EnrollmentJPA enrollment = enrollmentOptional.get();
        LessonJPA lesson = lessonOptional.get();

        return lesson.getModule().getCourse().getId().equals(enrollment.getCourse().getId());
    }
}