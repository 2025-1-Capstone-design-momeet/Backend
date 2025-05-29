package capstone2.backend.codes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Vote")
public class Vote {
    @Id
    @Column(name = "voteID", nullable = false, length = 32)
    private String voteID;

    @Column(name = "clubId", nullable = false, length = 32)
    private String clubId;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 100)
    private String content;

    @Column(name = "isAnonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "isPayed", nullable = false)
    private boolean isPayed;

    // 연관 매핑: clubId → Club
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId", referencedColumnName = "clubId", insertable = false, updatable = false)
    private Club club;

    // Vote ↔ VoteContent 역방향 관계 추가
    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<VoteContent> voteContents;
}
