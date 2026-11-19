package ijse.cmjd.cw3.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.CartItem;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndFoodItemId(Long cartId, Long foodItemId);

    void deleteByCartId(Long cartId);
}
