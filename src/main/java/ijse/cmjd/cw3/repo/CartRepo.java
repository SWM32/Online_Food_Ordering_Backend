package ijse.cmjd.cw3.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserEmail(String email);
}
