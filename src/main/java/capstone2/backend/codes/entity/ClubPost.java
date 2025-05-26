package capstone2.backend.codes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ClubPost")
@Data
@NoArgsConstructor
public class ClubPost {

    @Id
    @Column(name = "postNum")
    private String postNum;

    @OneToOne
    @JoinColumn(name = "postNum")
    @JsonIgnore // 🔥 순환 끊기
    private Post post;

    @ManyToOne
    @JoinColumn(name = "clubId", referencedColumnName = "clubId")
    @JsonIgnore // 🔥 순환 끊기 (클럽 전체 정보까지 내려보낼 필요 없으면)
    private Club club;

}
