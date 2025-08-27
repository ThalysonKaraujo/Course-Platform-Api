package com.thalyson.digitalcourses.course_platform_backend.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.validation.validator.ExistingInstructorValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingInstructorValidator.class)
public @interface ExistingIntructor {
    String message() default "O instrutor fornecido não existe ou não possui a role INSTRUCTOR";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
