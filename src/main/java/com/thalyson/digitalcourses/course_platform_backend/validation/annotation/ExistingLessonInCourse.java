package com.thalyson.digitalcourses.course_platform_backend.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.validation.validator.ExistingLessonInCourseValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistingLessonInCourseValidator.class)
public @interface ExistingLessonInCourse {
    String message() default "A aula fornecida não existe ou não pertence ao curso da matrícula.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
