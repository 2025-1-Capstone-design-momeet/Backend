package capstone2.backend.codes.repository;

import capstone2.backend.codes.dto.ClubSummaryDto;
import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.entity.ClubMembers;
import capstone2.backend.codes.entity.ClubMembersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMembersRepository extends JpaRepository<ClubMembers, ClubMembersId> {
    @Query(value = """
        SELECT * FROM Club c
        WHERE c.clubId IN (
            SELECT clubId FROM ClubMembers WHERE userId = :userId
            UNION
            SELECT clubId FROM Executive WHERE userId = :userId
            UNION
            SELECT clubId FROM President WHERE userId = :userId
        )
    """, nativeQuery = true)
    List<Club> findClubsByUserId(@Param("userId") String userId);


    @Query("""
        SELECT new capstone2.backend.codes.dto.ClubSummaryDto(
            c.clubId, c.clubName, c.category, c.isOfficial
        )
        FROM Club c
        WHERE c.clubId IN (
            SELECT cm.clubId FROM ClubMembers cm WHERE cm.userId = :userId
            UNION
            SELECT e.clubId FROM Executive e WHERE e.userId = :userId
            UNION
            SELECT p.clubId FROM President p WHERE p.userId = :userId
        )
    """)
    List<ClubSummaryDto> findClubSummariesByUserId(@Param("userId") String userId);
    int countByClubId(String clubId);

    List<ClubMembers> findByClubId(String clubId);
    boolean existsByUserIdAndClubId(String userId, String clubId);
}
