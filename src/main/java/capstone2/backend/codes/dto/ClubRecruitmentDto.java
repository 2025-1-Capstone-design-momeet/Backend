package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubRecruitmentDto {
    private String clubId;
    private String clubName;
    private String category;
    private boolean isOfficial;
    private LocalDateTime endDate;
    private boolean isRecruiting;
}
