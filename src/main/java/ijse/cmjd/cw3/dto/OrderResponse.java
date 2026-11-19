package ijse.cmjd.cw3.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ijse.cmjd.cw3.entity.OrderStatus;
import ijse.cmjd.cw3.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String deliveryAddress;
    private List<OrderItemResponse> items;
    private PaymentStatus paymentStatus;
}
