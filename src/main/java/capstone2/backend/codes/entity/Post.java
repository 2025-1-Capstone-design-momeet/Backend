package capstone2.backend.codes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Post")
public class Post {

    @Id
    @Column(name = "postNum", nullable = false)
    private String postNum;

    @Column(name = "title", nullable = false, length = 60)
    private String title;

    @Column(name = "content", length = 100)
    private String content;

    @Column(name = "type", nullable = false)
    private int type;

    @Column(name = "file", length = 2048)
    private String file;

    @Column(name = "`like`") // 예약어이므로 백틱 처리 (MySQL 기준)
    private int like;

    @Column(name = "fixaction")
    private int fixaction;

    @Column(name = "date")
    private LocalDateTime date;

    @OneToOne(mappedBy = "post")
    @JsonIgnore // 🔥 순환 끊기
    private ClubPost clubPost;

    @OneToOne(mappedBy = "post")
    @JsonIgnore // 🔥 순환 끊기
    private Poster poster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

}
