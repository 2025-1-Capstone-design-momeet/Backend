package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteWriteDto {
    private String clubId;
    private LocalDateTime endDate;
    private String title;
    private String content;
    private boolean anonymous;

    private List<VoteContentWriteDto> voteContents;
}
