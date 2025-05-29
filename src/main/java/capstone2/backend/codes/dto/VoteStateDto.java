package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteStateDto {
    private String userId;
    private String voteID;
    private int voteNum;
}
