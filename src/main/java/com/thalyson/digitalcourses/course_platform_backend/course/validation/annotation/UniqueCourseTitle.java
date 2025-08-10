package com.thalyson.digitalcourses.course_platform_backend.course.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.course.validation.validator.UniqueCourseTitleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCourseTitleValidator.class)
public @interface UniqueCourseTitle {

    String message() default "Já existe um curso com este título.";

    Class<?> [] groups() default  {};
    Class<? extends Payload>[] payload() default {};
}
