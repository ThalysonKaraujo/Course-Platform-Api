package com.thalyson.digitalcourses.course_platform_backend.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.repository.EnrollmentRepository;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingEnrollment;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ExistingEnrollmentValidator implements ConstraintValidator<ExistingEnrollment, Long> {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public boolean isValid(Long enrollmentId, ConstraintValidatorContext context) {
        if (enrollmentId == null) {
            return true;
        }
        return enrollmentRepository.existsById(enrollmentId);
    }

}
