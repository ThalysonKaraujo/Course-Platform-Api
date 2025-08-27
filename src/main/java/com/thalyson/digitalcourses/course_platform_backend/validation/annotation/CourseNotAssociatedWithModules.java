package com.thalyson.digitalcourses.course_platform_backend.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.validation.validator.CourseNotAssociatedWithModulesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CourseNotAssociatedWithModulesValidator.class)
public @interface CourseNotAssociatedWithModules {
    String message() default "Não é possível excluir o curso, pois ele possui módulos associados.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
