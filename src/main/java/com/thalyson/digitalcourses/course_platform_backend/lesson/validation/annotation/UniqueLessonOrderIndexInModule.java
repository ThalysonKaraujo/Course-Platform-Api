package com.thalyson.digitalcourses.course_platform_backend.lesson.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.lesson.validation.validator.UniqueLessonOrderIndexInModuleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueLessonOrderIndexInModuleValidator.class)
public @interface UniqueLessonOrderIndexInModule {
    String message() default "Já existe uma aula com esta ordem neste módulo.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
