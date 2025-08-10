package com.thalyson.digitalcourses.course_platform_backend.role.repository;

import com.thalyson.digitalcourses.course_platform_backend.role.jpa.RoleJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<RoleJPA, Long> {
    Optional<RoleJPA> findByName(String name);
}
