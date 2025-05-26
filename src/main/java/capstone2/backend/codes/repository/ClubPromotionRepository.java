package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.ClubPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubPromotionRepository extends JpaRepository<ClubPromotion, String> {
    @Query("""
    SELECT cp
    FROM ClubPromotion cp
    JOIN cp.club c
    WHERE c.univName = :univName
""")
    List<ClubPromotion> findAllByUnivName(@Param("univName") String univName);

}
