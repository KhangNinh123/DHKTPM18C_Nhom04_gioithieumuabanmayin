package com.iuh.printshop.printshop_be.service;

import com.iuh.printshop.printshop_be.entity.Category;
import com.iuh.printshop.printshop_be.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
        categoryRepository.findByName(category.getName()).ifPresent(existing -> {
            throw new RuntimeException("Category name already exists: " + category.getName());
        });

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> updateCategory(Integer id, Category updatedCategory) {
        return categoryRepository.findById(id).map(category -> {
            categoryRepository.findByName(updatedCategory.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Category name already exists: " + updatedCategory.getName());
                }
            });

            category.setName(updatedCategory.getName());
            category.setDescription(updatedCategory.getDescription());
            return categoryRepository.save(category);
        });
    }

    public boolean deleteCategory(Integer id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
}

