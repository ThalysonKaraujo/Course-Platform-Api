package com.thalyson.digitalcourses.course_platform_backend.repository;

import com.thalyson.digitalcourses.course_platform_backend.model.UserJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserJPA, Long> {
    Optional<UserJPA> findByEmail(String email);
}
