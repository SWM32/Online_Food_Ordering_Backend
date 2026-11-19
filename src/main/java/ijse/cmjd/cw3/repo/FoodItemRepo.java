package ijse.cmjd.cw3.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.FoodItem;
import ijse.cmjd.cw3.entity.FoodStatus;

@Repository
public interface FoodItemRepo extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByCategoryId(Long categoryId);

    List<FoodItem> findByStatus(FoodStatus status);

    List<FoodItem> findByCategoryIdAndStatus(Long categoryId, FoodStatus status);

    List<FoodItem> findByNameContainingIgnoreCase(String keyword);
}
