package com.thalyson.digitalcourses.course_platform_backend.category.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.course.validation.validator.ExistingCategoryValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingCategoryValidator.class)
public @interface ExistingCategory {
    String message() default "Categoria não encontrada com o ID fornecido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
