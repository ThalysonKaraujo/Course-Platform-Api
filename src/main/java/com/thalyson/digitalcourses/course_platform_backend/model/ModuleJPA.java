package com.thalyson.digitalcourses.course_platform_backend.model;

import com.thalyson.digitalcourses.course_platform_backend.dto.DadosAtualizacaoModule;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "modules")
public class ModuleJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseJPA course;

    private String title;

    private String description;

    private Integer orderIndex;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ModuleJPA() {
    }

    public ModuleJPA(CourseJPA course, String title, String description, Integer orderIndex) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public CourseJPA getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleJPA moduleJPA = (ModuleJPA) o;
        return Objects.equals(id, moduleJPA.id);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }



    public void updateFields(DadosAtualizacaoModule dados){
        if (dados.title() != null && !dados.title().isBlank()) {
            setTitle(dados.title());
        }
        if (dados.description() != null) {
            setDescription(dados.description());
        }
        if (dados.orderIndex() != null) {
            setOrderIndex(dados.orderIndex());
        }
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

}
