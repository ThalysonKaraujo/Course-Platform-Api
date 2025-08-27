package com.thalyson.digitalcourses.course_platform_backend.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.validation.validator.ExistingEnrollmentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingEnrollmentValidator.class)
public @interface ExistingEnrollment {
    String message() default "Matrícula não encontrada com o ID fornecido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
