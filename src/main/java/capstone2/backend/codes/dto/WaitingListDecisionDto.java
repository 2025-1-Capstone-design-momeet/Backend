package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitingListDecisionDto {
    private String userId;
    private String clubId;
    private String action;  // "approve" 또는 "reject"
}
