package com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.course.validation.validator.ExistingCategoryValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingCategoryValidator.class)
public @interface ExistingCategory {

    String message() default "A categoria fornecida não existe";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
