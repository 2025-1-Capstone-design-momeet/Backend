package capstone2.backend.codes.entity;

import capstone2.backend.codes.entity.VoteId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PaymentHistory")
public class PaymentHistory {
    @Id
    @Column(name = "payId", nullable = false, insertable = false, updatable = false)
    private String payId;

    @Column(name = "clubId", nullable = false, length = 32)
    private String clubId;

    @Column(name = "title", nullable = false, length = 32)
    private String title;

    @Column(name = "amount", nullable = false, length = 32)
    private int amount;

    @Column(name = "account", nullable = false, length = 32)
    private String account;

    // 연관 매핑: clubId → Club
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId", referencedColumnName = "clubId", insertable = false, updatable = false)
    private Club club;

}