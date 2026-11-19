package ijse.cmjd.cw3.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.OrderItem;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
}
