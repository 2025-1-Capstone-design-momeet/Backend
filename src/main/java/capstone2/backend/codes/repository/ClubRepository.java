package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, String> {
    //Optional<Club> findByClubId(String clubId);
}
