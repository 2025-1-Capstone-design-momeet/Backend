package capstone2.backend.codes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubPromotionDto {
    private String clubId;
    private String target;
    private int dues;
    private boolean interview;
    private LocalDateTime endDate;
    @JsonProperty("isRecruiting")
    private boolean isRecruiting;
}
