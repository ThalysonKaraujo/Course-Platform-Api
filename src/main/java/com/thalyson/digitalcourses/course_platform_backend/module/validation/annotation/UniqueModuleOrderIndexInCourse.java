package com.thalyson.digitalcourses.course_platform_backend.module.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.module.validation.validator.UniqueModuleOrderIndexInCourseValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueModuleOrderIndexInCourseValidator.class)
public @interface UniqueModuleOrderIndexInCourse {
    String message() default "Já existe um módulo com esta ordem neste curso.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
