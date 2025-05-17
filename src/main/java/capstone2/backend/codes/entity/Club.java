package capstone2.backend.codes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Club")
public class Club {
    @Id
    @Column(name = "clubId", nullable = false)
    private String clubId;

    @Column(name = "clubName", nullable = false)
    private String clubName;

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "managerId")
    private String managerId;

    @Column(name = "category", nullable = false)
    private int category;

    @Column (name = "bannerImage")
    private String bannerImage;

    @Column (name = "univName")
    private String univName;
}
