package com.example.InventoryManagementSystem.service;


import com.example.InventoryManagementSystem.dto.CategoryRequest;
import com.example.InventoryManagementSystem.dto.CategoryResponse;
import com.example.InventoryManagementSystem.model.Category;
import com.example.InventoryManagementSystem.Repository.CategoryRepository;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        return mapToResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());

        Category updated = categoryRepository.save(category);

        return mapToResponse(updated);
    }

    @Override
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {

        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .build();
    }
}