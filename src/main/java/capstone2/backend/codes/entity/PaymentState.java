package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(PaymentStateId.class)
@Table(name = "PaymentState")
public class PaymentState {
    @Id
    @Column(name = "payId", nullable = false, insertable = false, updatable = false)
    private String payId;

    @Id
    @Column(name = "userID", nullable = false)
    private String userId;


    @Column(name = "hasPaid", nullable = false)
    private boolean hasPaid = false;

    // 연관 매핑: payId → Pay
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payId", insertable = false, updatable = false)
    private PaymentHistory paymentHistory;

    // 연관 매핑: userId → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}
