package capstone2.backend.codes.dto;

import capstone2.backend.codes.entity.VoteContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetVoteDto {
    private String voteID;
    private String clubId;
    private LocalDateTime endDate;
    private boolean isEnd;
    private String title;
    private String content;
    private boolean isAnonymous;
    private boolean isPayed;

    private List<VoteContentDto> voteContets;



}
