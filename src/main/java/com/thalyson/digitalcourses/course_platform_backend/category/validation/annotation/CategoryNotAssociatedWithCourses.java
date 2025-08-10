package com.thalyson.digitalcourses.course_platform_backend.category.validation.annotation;

import com.thalyson.digitalcourses.course_platform_backend.category.validation.validation.CategoryNotAssociatedWithCoursesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryNotAssociatedWithCoursesValidator.class)
public @interface CategoryNotAssociatedWithCourses  {
    String message() default "Não é possível excluir a categoria, pois ela possui cursos associados."; // Mensagem padrão de erro

    Class<?>[] groups() default {}; // Necessário para validação em grupos

    Class<? extends Payload>[] payload() default {};
}
