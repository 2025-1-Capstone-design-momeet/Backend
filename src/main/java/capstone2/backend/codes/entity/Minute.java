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
@Table(name = "Minute")
public class Minute {
    @Id
    @Column(name = "minuteId", nullable = false, length = 32)
    private String minuteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "clubId", referencedColumnName = "clubId", nullable = false)
    private Club club;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "summaryContents", length = 1000)
    private String summaryContents;

    @Column(name = "filePath", length = 2048)
    private String filePath;
}

