package com.thalyson.digitalcourses.course_platform_backend.lesson.jpa;

import com.thalyson.digitalcourses.course_platform_backend.lesson.dto.DadosAtualizacaoLesson;
import com.thalyson.digitalcourses.course_platform_backend.module.jpa.ModuleJPA;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "lessons")
public class LessonJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleJPA module;

    private String title;

    private String description;

    private String youtubeVideoUrl;

    private Integer durationSeconds;

    private Integer orderIndex;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public LessonJPA() {
    }

    public LessonJPA(ModuleJPA module,
                     String title,
                     String description,
                     String youtubeVideoUrl,
                     Integer durationSeconds,
                     Integer orderIndex) {
        this.module = module;
        this.title = title;
        this.description = description;
        this.youtubeVideoUrl = youtubeVideoUrl;
        this.durationSeconds = durationSeconds;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public ModuleJPA getModule() {
        return module;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getYoutubeVideoUrl() {
        return youtubeVideoUrl;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setYoutubeVideoUrl(String youtubeVideoUrl) {
        this.youtubeVideoUrl = youtubeVideoUrl;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void updateFields(DadosAtualizacaoLesson dados) {
        if (dados.title() != null && !dados.title().isBlank()) {
            this.title = dados.title();
        }
        if (dados.description() != null) {
            this.description = dados.description();
        }
        if (dados.youtubeVideoUrl() != null && !dados.youtubeVideoUrl().isBlank()) {
            this.youtubeVideoUrl = dados.youtubeVideoUrl();
        }
        if (dados.durationSeconds() != null) {
            this.durationSeconds = dados.durationSeconds();
        }
        if (dados.orderIndex() != null) {
            this.orderIndex = dados.orderIndex();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LessonJPA lessonJPA = (LessonJPA) o;
        return Objects.equals(id, lessonJPA.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
