package com.thalyson.digitalcourses.course_platform_backend.category.validation.validation;

import com.thalyson.digitalcourses.course_platform_backend.category.validation.annotation.CategoryNotAssociatedWithCourses;
import com.thalyson.digitalcourses.course_platform_backend.course.repository.CourseRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CategoryNotAssociatedWithCoursesValidator implements ConstraintValidator<CategoryNotAssociatedWithCourses, Integer> {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public boolean isValid(Integer categoryId, ConstraintValidatorContext context){
        if (categoryId == null) {
            return true;
        }
        return !courseRepository.existsByCategoryId(categoryId);
    }
}
