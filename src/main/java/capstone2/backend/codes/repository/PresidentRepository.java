package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.President;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PresidentRepository extends JpaRepository<President, String> {
    Optional<President> findByClubId(String clubId);
    int countByClubId(String clubId);
}
