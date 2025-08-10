package com.thalyson.digitalcourses.course_platform_backend.lesson.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.lesson.validation.validator.UniqueLessonTitleInModuleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueLessonTitleInModuleValidator.class)
public @interface UniqueLessonTitleInModule {
    String message() default "Já existe uma aula com este título neste módulo.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
