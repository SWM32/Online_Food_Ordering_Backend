package ijse.cmjd.cw3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ijse.cmjd.cw3.dto.CategoryRequest;
import ijse.cmjd.cw3.dto.CategoryResponse;
import ijse.cmjd.cw3.entity.Category;
import ijse.cmjd.cw3.exception.DuplicateResourceException;
import ijse.cmjd.cw3.exception.ResourceNotFoundException;
import ijse.cmjd.cw3.repo.CategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service handling CRUD operations for food categories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepo.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepo.save(category);
        log.info("Created new category '{}' with id={}", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String newName = request.getName().trim();
        if (!category.getName().equalsIgnoreCase(newName) && categoryRepo.existsByNameIgnoreCase(newName)) {
            throw new DuplicateResourceException("Category already exists with name: " + newName);
        }

        category.setName(newName);
        category.setDescription(request.getDescription());

        Category updated = categoryRepo.save(category);
        log.info("Updated category id={} to name='{}'", updated.getId(), updated.getName());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepo.delete(category);
        log.info("Deleted category id={}", id);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
