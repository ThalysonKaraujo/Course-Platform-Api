package com.thalyson.digitalcourses.course_platform_backend.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingCourse;
import com.thalyson.digitalcourses.course_platform_backend.exception.ExistingResourceNotFoundException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistingCourseValidator implements ConstraintValidator<ExistingCourse, Long> {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public boolean isValid(Long courseId, ConstraintValidatorContext context) {
        if (courseId == null) {
            return true;
        }

        if (!courseRepository.existsById(courseId)) {
            throw new ExistingResourceNotFoundException("Curso não encontrado com id: " + courseId);
        }
        return true;
    }
}
