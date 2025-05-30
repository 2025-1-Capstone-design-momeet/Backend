package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(ClubMembershipFeeStateId.class)
@Table(name = "ClubMembershipFeeState")
public class ClubMembershipFeeState {
    @Id
    @Column(name = "membershipFeeId", nullable = false, insertable = false, updatable = false)
    private String membershipFeeId;

    @Id
    @Column(name = "userId", nullable = false)
    private String userId;


    @Column(name = "hasPaid", nullable = false)
    private boolean hasPaid = false;

    // 연관 매핑: payId → Pay
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membershipFeeId", insertable = false, updatable = false)
    private ClubMembershipFeeHistory clubMembershipFeeHistory;

    // 연관 매핑: userId → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}