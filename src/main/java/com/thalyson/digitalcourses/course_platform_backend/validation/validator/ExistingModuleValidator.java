package com.thalyson.digitalcourses.course_platform_backend.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.repository.ModuleRepository;
import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingModule;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExistingModuleValidator implements ConstraintValidator<ExistingModule, Long> {

    @Autowired
    private ModuleRepository moduleRepository;

    @Override
    public boolean isValid(Long moduleId, ConstraintValidatorContext context) {
        if (moduleId == null) {
            return true;
        }
        return moduleRepository.existsById(moduleId);
    }
}
