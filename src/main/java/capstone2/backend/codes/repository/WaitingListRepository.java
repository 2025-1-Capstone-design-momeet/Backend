package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.WaitingList;
import capstone2.backend.codes.entity.WaitingListId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitingListRepository extends JpaRepository<WaitingList, WaitingListId> {
    boolean existsByUserIdAndClubId(String userId, String clubId);
    Optional<WaitingList> findByUserIdAndClubId(String userId, String clubId);
    List<WaitingList> findByClubId(String clubId);
}