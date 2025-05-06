package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    //Optional<User>
}
