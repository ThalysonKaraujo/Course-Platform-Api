package com.thalyson.digitalcourses.course_platform_backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "enrollments", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "course_id"})})
public class EnrollmentJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJPA user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseJPA course;

    private LocalDateTime enrollmentDate;

    private String completionStatus;

    private BigDecimal progressPercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_Watched_lesson_id", nullable = true)
    private LessonJPA lastWatchedLesson;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "enrollment_completed_lessons", joinColumns = @JoinColumn(name = "enrollment_id"))
    @Column(name = "lesson_id")
    private Set<Long> completedLessonIds = new HashSet<>();

    public Set<Long> getCompletedLessonIds() {
        return this.completedLessonIds;
    }

    public void markLessonAsCompleted(Long lessonId){
        this.completedLessonIds.add(lessonId);
    }

    public EnrollmentJPA() {
        this.enrollmentDate = LocalDateTime.now();
        this.completionStatus = "IN_PROGRESS";
        this.progressPercentage = BigDecimal.ZERO;
    }

    public EnrollmentJPA(UserJPA user, CourseJPA course){
        this.user = user;
        this.course = course;
        this.enrollmentDate = LocalDateTime.now();
        this.completionStatus = "IN_PROGRESS";
        this.progressPercentage = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public UserJPA getUser() {
        return user;
    }

    public CourseJPA getCourse() {
        return course;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public LessonJPA getLastWatchedLesson() {
        return lastWatchedLesson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public void setLastWatchedLesson(LessonJPA lastWatchedLesson) {
        this.lastWatchedLesson = lastWatchedLesson;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnrollmentJPA that = (EnrollmentJPA) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
