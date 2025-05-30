package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.ClubMembershipFeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubMembershipFeeHistoryRepository extends JpaRepository<ClubMembershipFeeHistory, String> {
    Optional<ClubMembershipFeeHistory> findByClubIdAndTitle(String clubId, String title);

}