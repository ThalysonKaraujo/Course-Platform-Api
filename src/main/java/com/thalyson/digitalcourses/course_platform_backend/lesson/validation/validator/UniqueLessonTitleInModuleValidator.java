package com.thalyson.digitalcourses.course_platform_backend.lesson.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.lesson.repository.LessonRepository;
import com.thalyson.digitalcourses.course_platform_backend.lesson.validation.annotation.UniqueLessonTitleInModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UniqueLessonTitleInModuleValidator implements ConstraintValidator<UniqueLessonTitleInModule, String> {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        if (title == null || title.isBlank()) {
            return true;
        }

        Pattern p = Pattern.compile("/modules/(\\d+)/lessons");
        Matcher m = p.matcher(request.getRequestURI());
        Long moduleId = null;
        if (m.find()) {
            moduleId = Long.parseLong(m.group(1));
        }

        if (moduleId == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Não foi possível determinar o ID do módulo a partir da URL.")
                    .addConstraintViolation();
            return false;
        }

        String path = request.getRequestURI();
        Pattern pUpdate = Pattern.compile("/lessons/(\\d+)");
        Matcher mUpdate = pUpdate.matcher(path);
        Long lessonId = null;
        if (mUpdate.find()) {
            lessonId = Long.parseLong(mUpdate.group(1));
        }

        Optional<LessonJPA> existingLesson = lessonRepository.findByModuleIdAndTitle(moduleId, title);

        if (existingLesson.isPresent()) {
            if (lessonId != null && existingLesson.get().getId().equals(lessonId)) {
                return true;
            }
            return false;
        }

        return true;
    }

}


