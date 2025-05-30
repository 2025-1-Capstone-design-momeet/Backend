package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.ClubMembershipFeeHistory;
import capstone2.backend.codes.entity.ClubMembershipFeeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipFeeStateRepository  extends JpaRepository<ClubMembershipFeeState, String> {
    // 유저가 정산 미완료한 리스트
    @Query("SELECT ph FROM ClubMembershipFeeHistory ph " +
            "JOIN ClubMembershipFeeState ps ON ph.membershipFeeId = ps.membershipFeeId " +
            "WHERE ph.clubId = :clubId AND ps.userId = :userId AND ps.hasPaid = false")
    ClubMembershipFeeHistory findUnpaidPaymentsByUserInClub(@Param("clubId") String clubId,
                                                        @Param("userId") String userId);

    // 유저가 정산 완료한 리스트
    @Query("SELECT ph FROM ClubMembershipFeeHistory ph " +
            "JOIN ClubMembershipFeeState ps ON ph.membershipFeeId = ps.membershipFeeId " +
            "WHERE ph.clubId = :clubId AND ps.userId = :userId AND ps.hasPaid = true")
    ClubMembershipFeeHistory findPaidPaymentsByUserInClub(@Param("clubId") String clubId,
                                                      @Param("userId") String userId);
    // 모든 멤버가 정산 완료한 리스트
    @Query("""
    SELECT ph FROM ClubMembershipFeeHistory ph
    WHERE ph.clubId = :clubId
      AND NOT EXISTS (
          SELECT ps FROM ClubMembershipFeeState ps
          WHERE ps.membershipFeeId = ph.membershipFeeId
            AND ps.hasPaid = false
      )
    """)
    ClubMembershipFeeHistory findFullyPaidHistoriesByClubId(@Param("clubId") String clubId);

    // 모든 멤버가 아직 정산 하지 않은 값
    @Query("""
    SELECT ph FROM ClubMembershipFeeHistory ph
    WHERE ph.clubId = :clubId
      AND EXISTS (
          SELECT ps FROM ClubMembershipFeeState ps
          WHERE ps.membershipFeeId = ph.membershipFeeId
            AND ps.hasPaid = true
      )
    """)
    ClubMembershipFeeHistory findUnpaidByAllMembersInClub(@Param("clubId") String clubId);

    Optional<ClubMembershipFeeState> findByMembershipFeeIdAndUserId(String membershipFeeId, String userId);

    // 특정 payId에 해당하는 PaymentState 목록 조회
    List<ClubMembershipFeeState> findByMembershipFeeId(String membershipFeeId);
}