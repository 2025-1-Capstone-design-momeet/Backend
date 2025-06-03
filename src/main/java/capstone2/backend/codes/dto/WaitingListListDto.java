package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitingListListDto {
    private String userId;
    private String userName;
    private String department;
    private String studentNum;
    private String grade;
    private String why;
    private String what;
}
