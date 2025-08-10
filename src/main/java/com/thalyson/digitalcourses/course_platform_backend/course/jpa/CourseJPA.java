package com.thalyson.digitalcourses.course_platform_backend.course.jpa;

import com.thalyson.digitalcourses.course_platform_backend.category.jpa.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.course.dto.DadosAtualizacaoCourse;
import com.thalyson.digitalcourses.course_platform_backend.user.jpa.UserJPA;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "courses")
public class CourseJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String thumbnailUrl;

    private Boolean isPublished;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private UserJPA instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryJPA category;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public CourseJPA() {
    }

    public CourseJPA(String title, String description, String thumbnailUrl, Boolean isPublished, UserJPA instructor,
                     CategoryJPA category) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.isPublished = isPublished;
        this.instructor = instructor;
        this.category = category;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourseJPA that = (CourseJPA) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public UserJPA getInstructor() {
        return instructor;
    }

    public CategoryJPA getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setInstructor(UserJPA instructor) {
        this.instructor = instructor;
    }

    public void setCategory(CategoryJPA category) {
        this.category = category;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void updateFields(DadosAtualizacaoCourse dados) {
        if (dados.title() != null && !dados.title().isBlank()) {
            this.title = dados.title();
        }
        if (dados.description() != null) {
            this.description = dados.description();
        }
        if (dados.thumbnailUrl() != null) {
            this.thumbnailUrl = dados.thumbnailUrl();
        }
        if (dados.isPublished() != null) {
            this.isPublished = dados.isPublished();
        }
    }
}
