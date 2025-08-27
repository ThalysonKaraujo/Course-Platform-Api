package com.thalyson.digitalcourses.course_platform_backend.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.model.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.UniqueCategoryTitle;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UniqueCategoryTitleValidation implements ConstraintValidator<UniqueCategoryTitle, String> {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context){
        if (name == null || name.isBlank()){
            return true;
        }
        Optional<CategoryJPA> existingCategory = categoryRepository.findByNameIgnoreCase(name);
        return existingCategory.isEmpty();
    }

}
