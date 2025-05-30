package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubSummaryDto {
    private String clubId;
    private String clubName;
    private String category;
    private boolean isOfficial;
}
