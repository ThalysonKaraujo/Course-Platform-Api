package com.thalyson.digitalcourses.course_platform_backend.repository;

import com.thalyson.digitalcourses.course_platform_backend.model.CategoryJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryJPA, Integer> {
    Optional<CategoryJPA> findByName(String name);

    Optional<CategoryJPA> findByNameIgnoreCase(String name);
}
