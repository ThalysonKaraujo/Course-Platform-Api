package com.thalyson.digitalcourses.course_platform_backend.validation.validator;

import com.thalyson.digitalcourses.course_platform_backend.validation.annotation.ExistingIntructor;
import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExistingInstructorValidator implements ConstraintValidator<ExistingIntructor, Long> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(Long instructorId, ConstraintValidatorContext context){
        if (instructorId == null) {
            return true;
        }

        Optional<UserJPA> userOptional = userRepository.findById(instructorId);
        if (userOptional.isEmpty()){
            return false;
        }

        UserJPA user = userOptional.get();
        boolean hasRequiredRole = user.getAuthorities().stream()
                .anyMatch(auth ->
                        auth.getAuthority().equals("ROLE_INSTRUCTOR") || auth.getAuthority().equals("ROLE_ADMIN"));

        return hasRequiredRole;
    }
}
