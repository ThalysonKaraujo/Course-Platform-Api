package com.thalyson.digitalcourses.course_platform_backend.repository;

import com.thalyson.digitalcourses.course_platform_backend.model.CourseJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseJPA, Long> {
    Optional<CourseJPA> findByTitleIgnoreCase(String title);
    boolean existsByCategoryId(Integer categoryId);
}
