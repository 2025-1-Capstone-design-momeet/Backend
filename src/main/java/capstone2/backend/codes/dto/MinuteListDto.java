package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MinuteListDto {
    private String minuteId;
    private String title;
    private LocalDateTime date;
}
