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
@Table(name = "Club")
public class Club {

    @Id
    @Column(name = "clubId", nullable = false)
    private String clubId;

    @Column(name = "clubName", nullable = false)
    private String clubName;

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "managerId")
    private String managerId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "bannerImage")
    private String bannerImage;

    @Column(name = "univName")
    private String univName;

    @Column(name = "isOfficial", nullable = false)
    private boolean isOfficial; // 공식 동아리 여부 (1: 공식, 0: 비공식)

    // Club ↔ ClubPost 역방향 관계 추가
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubPost> clubPosts;

    // Club ↔ clubPromotion 역방향 관계 추가
    @OneToOne(mappedBy = "club")
    private ClubPromotion clubPromotion;

}
