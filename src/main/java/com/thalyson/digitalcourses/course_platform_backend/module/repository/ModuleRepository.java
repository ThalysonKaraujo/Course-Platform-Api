package com.thalyson.digitalcourses.course_platform_backend.module.repository;

import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<ModuleJPA, Long> {

    Optional<ModuleJPA> findByCourseAndTitle(CourseJPA course, String title);
    boolean existsByCourseId(Long courseId);
    List<ModuleJPA> findByCourse(CourseJPA course);
    Optional<ModuleJPA> findByTitleIgnoreCase(String title);
    @Query("SELECT m FROM ModuleJPA m WHERE m.course.id = :courseId AND m.orderIndex = :orderIndex")
    Optional<ModuleJPA> findByCourseIdAndOrderIndex(@Param("courseId") Long courseId, @Param("orderIndex") Integer orderIndex);
}
