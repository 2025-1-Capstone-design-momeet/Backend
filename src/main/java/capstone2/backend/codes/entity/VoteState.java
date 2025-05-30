package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(VoteId.class)
@Table(name = "VoteState")
public class VoteState {
    @Id
    @Column(name = "userId", nullable = false, insertable = false, updatable = false)
    private String userId;

    @Id
    @Column(name = "voteID", nullable = false)
    private String voteID;

    @Id
    @Column(name = "voteContentId", nullable = false, insertable = false, updatable = false)
    private String voteContentId;


    @Column(name = "voteNum", nullable = false)
    private int voteNum;



    // 연관 매핑: userId → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    // 연관 매핑: voteID → Vote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voteID", insertable = false, updatable = false)
    private Vote vote;

    // 연관 매핑: payID → PaymentHistory
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voteContentID", insertable = false, updatable = false)
    private VoteContent voteContent;
}
