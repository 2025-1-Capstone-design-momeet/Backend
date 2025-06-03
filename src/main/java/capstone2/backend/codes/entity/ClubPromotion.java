package capstone2.backend.codes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ClubPromotion")
public class ClubPromotion {

    @Id
    @Column(name = "clubId", nullable = false)
    private String clubId;

    @MapsId
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "clubId", referencedColumnName = "clubId") // FK → Club.clubId
    private Club club;

    @Column(name = "target", nullable = false)
    private String target;

    @Column(name = "dues")
    private int dues;

    @Column(name = "interview", nullable = false)
    private boolean interview;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @Column(name = "isRecruiting", nullable = false)
    private boolean isRecruiting;
}
