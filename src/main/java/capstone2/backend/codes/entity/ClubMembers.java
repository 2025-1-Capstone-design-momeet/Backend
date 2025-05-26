package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    🧠 멍청이(나)를 위한 설명:

    ClubMembers는 "어떤 유저가 어떤 클럽에 가입했는지" 나타내는 테이블이다.

    User(userId)  ←── ClubMembers (userId + clubNum) ──→ Club(clubId)
                            ↑ 2개로 이루어진 복합키 = ClubMembersId
                            ↑ JPA에서 연관관계까지 연결돼 있음

    이 말인즉슨...
    - userId는 User 테이블의 PK
    - clubNum은 Club 테이블의 PK (clubId랑 연결됨)
    - 이 둘을 묶어서 ClubMembers의 기본키로 사용함

    그리고 이 파일에서 하는 일은...
    - ClubMembersId를 복합키로 지정 (@IdClass)
    - @ManyToOne으로 User와 Club을 JPA에서 연결해줌 (즉, getUser().getName() 이런 거 가능!)
    - insertable = false / updatable = false 를 써서 충돌 안 나게 막아줌
*/

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(ClubMembersId.class)
@Table(name = "ClubMembers")
public class ClubMembers {
    @Id
    @Column(name = "userId", nullable = false, insertable = false, updatable = false)
    private String userId;

    @Id
    @Column(name = "clubId", nullable = false, insertable = false, updatable = false)
    private String clubId;

    @Column(name = "role")
    private String role;

    // 연관 매핑: userId → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    private User user;

    // 연관 매핑: clubId → Club
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clubId", referencedColumnName = "clubId", insertable = false, updatable = false)
    private Club club;
}
