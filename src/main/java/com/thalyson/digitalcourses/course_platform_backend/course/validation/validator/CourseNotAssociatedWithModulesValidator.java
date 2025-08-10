package com.thalyson.digitalcourses.course_platform_backend.course.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation.CourseNotAssociatedWithModules;
import com.thalyson.digitalcourses.course_platform_backend.module.repository.ModuleRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CourseNotAssociatedWithModulesValidator implements ConstraintValidator<CourseNotAssociatedWithModules, Long> {

    @Autowired
    private ModuleRepository moduleRepository;

    @Override
    public boolean isValid(Long courseId, ConstraintValidatorContext context) {
        if (courseId == null) {
            return true;
        }

        return !moduleRepository.existsByCourseId(courseId);
    }
}
