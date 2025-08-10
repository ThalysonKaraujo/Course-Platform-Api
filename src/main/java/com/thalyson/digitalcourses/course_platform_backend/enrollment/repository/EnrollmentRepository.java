package com.thalyson.digitalcourses.course_platform_backend.enrollment.repository;

import com.thalyson.digitalcourses.course_platform_backend.course.jpa.CourseJPA;
import com.thalyson.digitalcourses.course_platform_backend.enrollment.jpa.EnrollmentJPA;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<EnrollmentJPA, Long> {
    Optional<EnrollmentJPA> findByUserAndCourse(UserJPA user, CourseJPA course);
    List<EnrollmentJPA> findByUser(UserJPA user);
    List<EnrollmentJPA> findByCourse(CourseJPA course);
}
