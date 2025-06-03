package capstone2.backend.codes.repository;

import capstone2.backend.codes.dto.ClubRecruitmentDto;
import capstone2.backend.codes.entity.ClubPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubPromotionRepository extends JpaRepository<ClubPromotion, String> {
    @Query("""
    SELECT cp
    FROM ClubPromotion cp
    JOIN cp.club c
    WHERE c.univName = :univName
""")
    List<ClubPromotion> findAllByUnivName(@Param("univName") String univName);
    @Query("""
        SELECT new capstone2.backend.codes.dto.ClubRecruitmentDto(
            c.clubId,
            c.clubName,
            c.category,
            c.isOfficial,
            cp.endDate,
            cp.isRecruiting
        )
        FROM ClubPromotion cp
        JOIN cp.club c
        WHERE c.univName = :univName
        ORDER BY cp.isRecruiting DESC, cp.endDate ASC
    """)
    List<ClubRecruitmentDto> findRecruitingClubsByUnivName(@Param("univName") String univName);
    Optional<ClubPromotion> findByClubId(String clubId);
}
