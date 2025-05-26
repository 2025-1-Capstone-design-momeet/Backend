package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.entity.ClubMembers;
import capstone2.backend.codes.entity.ClubMembersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubMembersRepository extends JpaRepository<ClubMembers, ClubMembersId> {

    @Query("""
        SELECT c
        FROM ClubMembers cm
        JOIN Club c ON cm.clubId = c.clubId
        WHERE cm.userId = :userId
    """)
    List<Club> findClubsByUserId(@Param("userId") String userId);
}
