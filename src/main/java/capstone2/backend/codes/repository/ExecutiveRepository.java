package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.ClubMembersId;
import capstone2.backend.codes.entity.Executive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutiveRepository extends JpaRepository<Executive, ClubMembersId> {
    List<Executive> findByClubId(String clubId);
    int countByClubId(String clubId);
    boolean existsByUserIdAndClubId(String userId, String clubId);
}
