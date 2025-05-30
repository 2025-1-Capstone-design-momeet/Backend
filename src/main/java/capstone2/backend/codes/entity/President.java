package capstone2.backend.codes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "President")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class President {

    @Id
    @Column(name = "clubId")
    private String clubId;

    @Column(name = "userId", nullable = false)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    private User user;
}
