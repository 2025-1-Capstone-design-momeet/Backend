package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(WaitingListId.class)
@Table(name = "WaitingList")
public class WaitingList {

    @Id
    @Column(name = "userId", nullable = false)
    private String userId;

    @Id
    @Column(name = "clubId", nullable = false)
    private String clubId;

    @Column(name = "why")
    private String why;

    @Column(name = "what")
    private String what;

    // 연관 매핑을 추가하고 싶다면 이렇게도 가능 (선택)
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "clubId", referencedColumnName = "clubId", insertable = false, updatable = false)
    // private Club club;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    // private User user;
}
