package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentStateRepository extends JpaRepository<PaymentState, String> {
    // 유저가 정산 미완료한 리스트
    @Query("SELECT ph FROM PaymentHistory ph " +
            "JOIN PaymentState ps ON ph.payId = ps.payId " +
            "WHERE ph.clubId = :clubId AND ps.userId = :userId AND ps.hasPaid = false")
    List<PaymentHistory> findUnpaidPaymentsByUserInClub(@Param("clubId") String clubId,
                                                        @Param("userId") String userId);

    // 유저가 정산 완료한 리스트
    @Query("SELECT ph FROM PaymentHistory ph " +
            "JOIN PaymentState ps ON ph.payId = ps.payId " +
            "WHERE ph.clubId = :clubId AND ps.userId = :userId AND ps.hasPaid = true")
    List<PaymentHistory> findPaidPaymentsByUserInClub(@Param("clubId") String clubId,
                                                        @Param("userId") String userId);
    // 모든 멤버가 정산 완료한 리스트
    @Query("""
    SELECT ph FROM PaymentHistory ph
    WHERE ph.clubId = :clubId
      AND NOT EXISTS (
          SELECT ps FROM PaymentState ps
          WHERE ps.payId = ph.payId
            AND ps.hasPaid = false
      )
    """)
    List<PaymentHistory> findFullyPaidHistoriesByClubId(@Param("clubId") String clubId);

    // 모든 멤버가 아직 정산 하지 않은 리스트
    @Query("""
    SELECT ph FROM PaymentHistory ph
    WHERE ph.clubId = :clubId
      AND NOT EXISTS (
          SELECT ps FROM PaymentState ps
          WHERE ps.payId = ph.payId
            AND ps.hasPaid = true
      )
    """)
    List<PaymentHistory> findUnpaidByAllMembersInClub(@Param("clubId") String clubId);

    Optional<PaymentState> findByPayIdAndUserId(String payId, String userId);

    // 특정 payId에 해당하는 PaymentState 목록 조회
    List<PaymentState> findByPayId(String payId);
}