package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubPromotionDetailDto {
    private String clubName;
    private String category;
    private String target;
    private int dues;
    private boolean interview;
    private LocalDateTime endDate;
    private boolean isRecruiting;
}
