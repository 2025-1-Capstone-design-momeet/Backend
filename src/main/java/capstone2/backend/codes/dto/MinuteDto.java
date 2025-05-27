package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinuteDto {
    private String minuteId;
    private String clubId;
    private LocalDateTime date;
    private String summaryContents;
}
