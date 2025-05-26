package capstone2.backend.codes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @OneToOne
    @JoinColumn(name = "postNum", referencedColumnName = "postNum")
    @JsonIgnore // 🔥 순환 끊기 (또는 DTO로 전환 시 제거 가능)
    private Post post;

}