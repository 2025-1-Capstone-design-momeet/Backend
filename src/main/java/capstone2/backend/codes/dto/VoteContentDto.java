package capstone2.backend.codes.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteContentDto {
    private String voteContentID;
    private String voteID;
    private String field;
    private int voteNum;
    private int voteContentNum;
}
