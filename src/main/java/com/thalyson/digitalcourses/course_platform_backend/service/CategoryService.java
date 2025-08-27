package com.thalyson.digitalcourses.course_platform_backend.service;

import com.thalyson.digitalcourses.course_platform_backend.dto.DadosCadastroCategory;
import com.thalyson.digitalcourses.course_platform_backend.model.CategoryJPA;
import com.thalyson.digitalcourses.course_platform_backend.repository.CategoryRepository;
import com.thalyson.digitalcourses.course_platform_backend.exception.DuplicateResourceException;
import com.thalyson.digitalcourses.course_platform_backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public CategoryJPA create(DadosCadastroCategory dados) {

        if (categoryRepository.findByName(dados.name()).isPresent()) {
            throw new DuplicateResourceException("Uma categoria com o nome '" + dados.name() + "' já existe.");
        }

        CategoryJPA newCategory = new CategoryJPA(dados.name());
        return categoryRepository.save(newCategory);
    }

    public Optional<CategoryJPA> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public List<CategoryJPA> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public CategoryJPA update(Integer id, DadosCadastroCategory dados) {
        CategoryJPA existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + id));

        Optional<CategoryJPA> categoryWithSameName = categoryRepository.findByNameIgnoreCase(dados.name());
        if (categoryWithSameName.isPresent() && !categoryWithSameName.get().getId().equals(existingCategory.getId())) {
            throw new DuplicateResourceException("Já existe outra categoria com este nome: '" + dados.name() + "'.");
        }

        existingCategory.setName(dados.name());
        return categoryRepository.save(existingCategory);
    }

    @Transactional
    public void delete(Integer id) {
        CategoryJPA category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria não encontrada com ID: " + id));
        categoryRepository.delete(category);
    }
}