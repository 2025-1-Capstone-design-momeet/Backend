package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ClubMembershipFeeHistory")
public class ClubMembershipFeeHistory {
    @Id
    @Column(name = "membershipFeeId", nullable = false, insertable = false, updatable = false)
    private String membershipFeeId;

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
