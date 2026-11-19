package ijse.cmjd.cw3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ijse.cmjd.cw3.dto.FoodItemRequest;
import ijse.cmjd.cw3.dto.FoodItemResponse;
import ijse.cmjd.cw3.entity.Category;
import ijse.cmjd.cw3.entity.FoodItem;
import ijse.cmjd.cw3.entity.FoodStatus;
import ijse.cmjd.cw3.exception.ResourceNotFoundException;
import ijse.cmjd.cw3.repo.CategoryRepo;
import ijse.cmjd.cw3.repo.FoodItemRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service handling CRUD and query operations for food items.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodItemRepo foodItemRepo;
    private final CategoryRepo categoryRepo;

    @Transactional
    public FoodItemResponse createFoodItem(FoodItemRequest request) {
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        FoodItem foodItem = FoodItem.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : FoodStatus.AVAILABLE)
                .category(category)
                .build();

        FoodItem saved = foodItemRepo.save(foodItem);
        log.info("Created food item '{}' (id={}) under category '{}'",
                saved.getName(), saved.getId(), category.getName());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> getAllFoodItems() {
        return foodItemRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FoodItemResponse getFoodItemById(Long id) {
        FoodItem foodItem = foodItemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
        return mapToResponse(foodItem);
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByCategory(Long categoryId) {
        if (!categoryRepo.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return foodItemRepo.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> getAvailableFoodItems() {
        return foodItemRepo.findByStatus(FoodStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> searchFoodItems(String keyword) {
        return foodItemRepo.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public FoodItemResponse updateFoodItem(Long id, FoodItemRequest request) {
        FoodItem foodItem = foodItemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        foodItem.setName(request.getName().trim());
        foodItem.setDescription(request.getDescription());
        foodItem.setPrice(request.getPrice());
        if (request.getStatus() != null) {
            foodItem.setStatus(request.getStatus());
        }
        foodItem.setCategory(category);

        FoodItem updated = foodItemRepo.save(foodItem);
        log.info("Updated food item id={} ('{}', status={})",
                updated.getId(), updated.getName(), updated.getStatus());
        return mapToResponse(updated);
    }

    @Transactional
    public FoodItemResponse updateFoodStatus(Long id, FoodStatus status) {
        FoodItem foodItem = foodItemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
        foodItem.setStatus(status);
        FoodItem updated = foodItemRepo.save(foodItem);
        log.info("Updated status of food item id={} to {}", id, status);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteFoodItem(Long id) {
        FoodItem foodItem = foodItemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
        foodItemRepo.delete(foodItem);
        log.info("Deleted food item id={}", id);
    }

    private FoodItemResponse mapToResponse(FoodItem foodItem) {
        Category category = foodItem.getCategory();
        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .price(foodItem.getPrice())
                .status(foodItem.getStatus())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .build();
    }
}
