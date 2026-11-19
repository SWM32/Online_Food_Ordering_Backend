package ijse.cmjd.cw3.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ijse.cmjd.cw3.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paymentDate;
}
