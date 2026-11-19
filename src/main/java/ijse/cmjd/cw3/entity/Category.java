package ijse.cmjd.cw3.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a food category that groups multiple menu food items.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    // One Category has many Food Items
    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodItem> foodItems = new ArrayList<>();

    // Helper methods to keep both sides of the bidirectional relationship in sync
    public void addFoodItem(FoodItem foodItem) {
        foodItems.add(foodItem);
        foodItem.setCategory(this);
    }

    public void removeFoodItem(FoodItem foodItem) {
        foodItems.remove(foodItem);
        foodItem.setCategory(null);
    }
}
