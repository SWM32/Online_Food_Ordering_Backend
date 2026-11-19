package ijse.cmjd.cw3.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long foodItemId;
    private String foodItemName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
