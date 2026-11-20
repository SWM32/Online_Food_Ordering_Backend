package ijse.cmjd.cw3.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ijse.cmjd.cw3.dto.FoodItemRequest;
import ijse.cmjd.cw3.dto.FoodItemResponse;
import ijse.cmjd.cw3.entity.FoodStatus;
import ijse.cmjd.cw3.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for CRUD and search operations on food items.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodItemResponse>> getAllFoodItems() {
        return ResponseEntity.ok(foodService.getAllFoodItems());
    }

    @GetMapping("/available")
    public ResponseEntity<List<FoodItemResponse>> getAvailableFoodItems() {
        return ResponseEntity.ok(foodService.getAvailableFoodItems());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<FoodItemResponse>> getFoodItemsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(foodService.getFoodItemsByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodItemResponse>> searchFoodItems(@RequestParam String keyword) {
        return ResponseEntity.ok(foodService.searchFoodItems(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodItemResponse> getFoodItemById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodItemById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FoodItemResponse> createFoodItem(@Valid @RequestBody FoodItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodService.createFoodItem(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FoodItemResponse> updateFoodItem(
            @PathVariable Long id,
            @Valid @RequestBody FoodItemRequest request) {
        return ResponseEntity.ok(foodService.updateFoodItem(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<FoodItemResponse> updateFoodStatus(
            @PathVariable Long id,
            @RequestParam FoodStatus status) {
        return ResponseEntity.ok(foodService.updateFoodStatus(id, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        foodService.deleteFoodItem(id);
        return ResponseEntity.noContent().build();
    }
}
