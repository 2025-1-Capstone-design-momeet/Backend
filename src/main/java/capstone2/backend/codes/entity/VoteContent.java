package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "VoteContent")
public class VoteContent {

    @Id
    @Column(name = "voteContentID", nullable = false)
    private String voteContentID;

    @Column(name = "voteID", nullable = false)
    private String voteID;

    @Column(name = "field", nullable = false)
    private String field;

    @Column(name = "voteNum", nullable = false)
    private int voteNum;

    // 연관 매핑: voteID → Vote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voteID", insertable = false, updatable = false)
    @ToString.Exclude
    private Vote vote;
}