package com.thalyson.digitalcourses.course_platform_backend.course.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation.UniqueCourseTitle;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UniqueCourseTitleValidator implements ConstraintValidator<UniqueCourseTitle, String> {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context){
        if (title == null || title.isBlank()) {
            return true;
        }

        Optional<CourseJPA> existingCourse = courseRepository.findByTitleIgnoreCase(title);
        return existingCourse.isEmpty();
    }

}
