package com.thalyson.digitalcourses.course_platform_backend.user.repository;

import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserJPA, Long> {
    Optional<UserJPA> findByEmail(String email);
}
