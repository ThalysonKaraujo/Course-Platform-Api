package com.thalyson.digitalcourses.course_platform_backend.module.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.module.validation.validator.UniqueModuleTitleInCourseValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueModuleTitleInCourseValidator.class)
public @interface UniqueModuleTitleInCourse {
    String message() default "Já existe um módulo com este título neste curso.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
