package com.thalyson.digitalcourses.course_platform_backend.module.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.module.validation.annotation.UniqueModuleOrderIndexInCourse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UniqueModuleOrderIndexInCourseValidator implements ConstraintValidator<UniqueModuleOrderIndexInCourse, Integer> {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean isValid(Integer orderIndex, ConstraintValidatorContext context) {
        if (orderIndex == null) {
            return true;
        }

        Pattern p = Pattern.compile("/courses/(\\d+)/modules");
        Matcher m = p.matcher(request.getRequestURI());
        Long courseId = null;
        if (m.find()) {
            courseId = Long.parseLong(m.group(1));
        }

        if (courseId == null) {
            return false;
        }

        Optional<ModuleJPA> existingModule = moduleRepository.findByCourseIdAndOrderIndex(courseId, orderIndex);
        return existingModule.isEmpty();
    }
}
