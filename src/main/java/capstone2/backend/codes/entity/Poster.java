package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Poster")
public class Poster {
    @Id
    @Column(name = "postNum", nullable = false)
    private String postNum;
    @Column(name = "img", length = 2048)
    private String img;

    @MapsId
    @OneToOne   // Poster.postNum == Post.postNum
    @JoinColumn(name = "postNum", referencedColumnName = "postNum")
    private Post post;
}