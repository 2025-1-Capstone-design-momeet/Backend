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
    @Column(name = "postNum", nullable = false)
    private String postNum;

    @MapsId  // ✅ Post의 postNum을 ID로 사용
    @OneToOne
    @JoinColumn(name = "postNum")
    @JsonIgnore
    private Post post;

    @ManyToOne
    @JoinColumn(name = "clubId", referencedColumnName = "clubId")
    @JsonIgnore
    private Club club;
}

