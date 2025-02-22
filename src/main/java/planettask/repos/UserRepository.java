package planettask.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import planettask.domain.User;


public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmailIgnoreCase(String email);

}
