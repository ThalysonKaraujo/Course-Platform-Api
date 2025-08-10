package com.thalyson.digitalcourses.course_platform_backend.category.validation.validation;

import com.thalyson.digitalcourses.course_platform_backend.category.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation.ExistingCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistingCategoryValidator implements ConstraintValidator<ExistingCategory, Integer> {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public boolean isValid(Integer categoryId, ConstraintValidatorContext context) {
        if (categoryId == null) {
            return true;
        }
        return categoryRepository.existsById(categoryId);
    }
}
