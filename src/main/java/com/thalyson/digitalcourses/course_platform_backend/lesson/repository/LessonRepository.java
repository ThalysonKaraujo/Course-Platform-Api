package com.thalyson.digitalcourses.course_platform_backend.lesson.repository;

import com.thalyson.digitalcourses.course_platform_backend.lesson.jpa.LessonJPA;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<LessonJPA, Long> {
    Optional<LessonJPA> findByYoutubeVideoUrl(String youtubeVideoUrl);
    Optional<LessonJPA> findByModuleAndOrderIndex(ModuleJPA module, Integer orderIndex);
    List<LessonJPA> findByModule(ModuleJPA module);
    @Query("SELECT l FROM LessonJPA l WHERE l.module.id = :moduleId AND l.title = :title")
    Optional<LessonJPA> findByModuleIdAndTitle(@Param("moduleId") Long moduleId, @Param("title") String title);
    @Query("SELECT l FROM LessonJPA l WHERE l.module.id = :moduleId AND l.orderIndex = :orderIndex")
    Optional<LessonJPA> findByModuleIdAndOrderIndex(@Param("moduleId") Long moduleId, @Param("orderIndex") Integer orderIndex);
    @Query("SELECT COUNT(l) FROM LessonJPA l WHERE l.module.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}
