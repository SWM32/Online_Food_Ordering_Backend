package ijse.cmjd.cw3.dto;

import java.math.BigDecimal;

import ijse.cmjd.cw3.entity.FoodStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private FoodStatus status;
    private Long categoryId;
    private String categoryName;
}
