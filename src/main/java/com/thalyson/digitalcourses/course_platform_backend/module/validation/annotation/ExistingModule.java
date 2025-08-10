package com.thalyson.digitalcourses.course_platform_backend.module.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.module.validation.validator.ExistingModuleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingModuleValidator.class)
public @interface ExistingModule {
    String message() default "Módulo não encontrado com o ID fornecido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
