package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, String> {
    @Query("""
        SELECT c
        FROM Club c
        WHERE c.univName LIKE :univName
    """)
    List<Club> findAllByUnivName(@Param("univName") String univName);
    boolean existsByClubNameAndUnivName(String clubName, String univName);
}
