package ijse.cmjd.cw3.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
