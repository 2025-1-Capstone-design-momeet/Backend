package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MinuteDto {
    private String minuteId;
    private LocalDateTime date;
    private String title;              // 회의 제목
    private String summary;            // 회의 요약
    private String filePath;
    private List<ScriptLine> script;
}

