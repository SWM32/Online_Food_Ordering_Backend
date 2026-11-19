package ijse.cmjd.cw3.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.Order;
import ijse.cmjd.cw3.entity.OrderStatus;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByUserEmailOrderByOrderDateDesc(String email);

    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    List<Order> findAllByOrderByOrderDateDesc();
}
