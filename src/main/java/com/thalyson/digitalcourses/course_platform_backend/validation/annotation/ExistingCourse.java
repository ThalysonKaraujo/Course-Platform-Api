package com.thalyson.digitalcourses.course_platform_backend.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.validation.validator.ExistingCourseValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingCourseValidator.class)
public @interface ExistingCourse {
    String message() default "Curso não encontrado com o ID fornecido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
