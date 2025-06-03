package capstone2.backend.codes.repository;

import capstone2.backend.codes.entity.PaymentHistory;
import capstone2.backend.codes.entity.PaymentState;
import capstone2.backend.codes.entity.PersonalNotification;
import capstone2.backend.codes.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PersonalNotificationRepository extends JpaRepository<PersonalNotification, String> {
    List<PersonalNotification> findByUserUserId(String userId);

    // 특정 유저와 정산 정보를 기반으로 알림 삭제
    void deleteByUserAndPaymentHistory(User user, PaymentHistory paymentHistory);


}
