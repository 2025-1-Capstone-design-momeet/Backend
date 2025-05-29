package capstone2.backend.codes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Calendar")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calendar {
    @Id
    @Column(name = "scheduleId", nullable = false)
    private String scheduleId;
    @Column(name = "clubId", nullable = false)
    private String clubId;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "time")
    private LocalTime time;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "content", nullable = false)
    private String content;
}
