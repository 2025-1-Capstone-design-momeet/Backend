package capstone2.backend.codes.entity;

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
    @JoinColumn(name = "postNum") // FK → Post.postNum
    private Post post;

    @ManyToOne
    @JoinColumn(name = "clubId", referencedColumnName = "clubId") // ✅ FK → Club.clubId
    private Club club;
}
