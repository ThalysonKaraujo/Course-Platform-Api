package com.thalyson.digitalcourses.course_platform_backend.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.validation.validator.UniqueCategoryTitleValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCategoryTitleValidation.class)
public @interface UniqueCategoryTitle {
    String message() default "Já existe uma categoria com esse título";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
