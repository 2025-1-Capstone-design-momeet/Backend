package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Executive")
@IdClass(ClubMembersId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Executive {

    @Id
    @Column(name = "userId")
    private String userId;

    @Id
    @Column(name = "clubId")
    private String clubId;

    @Column(name = "duty")
    private String duty;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}

