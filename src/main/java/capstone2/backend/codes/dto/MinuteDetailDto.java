package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinuteDetailDto {
    private String minuteId;
    private LocalDateTime date;
    private String title;
    private String summary;
    private List<ScriptLine> script;
}
