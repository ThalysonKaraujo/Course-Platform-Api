package com.thalyson.digitalcourses.course_platform_backend.module.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.validation.annotation.UniqueModuleTitleInCourse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class UniqueModuleTitleInCourseValidator implements ConstraintValidator<UniqueModuleTitleInCourse, String> {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        if (title == null || title.isBlank()) {
            return true;
        }

        Pattern p = Pattern.compile("/modules/(\\d+)");
        Matcher m = p.matcher(request.getRequestURI());
        Long moduleId = null;
        if (m.find()) {
            moduleId = Long.parseLong(m.group(1));
        }

        Optional<ModuleJPA> existingModule = moduleRepository.findByTitleIgnoreCase(title);
        if (existingModule.isPresent()) {
            return existingModule.get().getId().equals(moduleId);
        }
        return true;
    }
}
