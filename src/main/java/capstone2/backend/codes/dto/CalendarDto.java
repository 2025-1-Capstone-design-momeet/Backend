package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarDto {
    private String clubId;
    private LocalDate date;
    private LocalTime time;
    private String title;
    private String content;
}