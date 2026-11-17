package ijse.cmjd.cw3.repo;

import org.springframework.stereotype.Repository;

import ijse.cmjd.cw3.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
