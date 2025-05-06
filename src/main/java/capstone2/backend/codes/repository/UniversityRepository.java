package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, String> {
    //Optional<User>
}
