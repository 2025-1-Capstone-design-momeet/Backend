package capstone2.backend.codes.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteContentWriteDto {
    private String  field;
    private int voteNum;
}
