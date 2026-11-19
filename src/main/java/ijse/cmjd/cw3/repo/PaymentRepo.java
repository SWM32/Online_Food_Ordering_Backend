package ijse.cmjd.cw3.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.Payment;
import ijse.cmjd.cw3.entity.PaymentStatus;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);
}
