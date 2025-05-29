package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentPostDto {
    private String postNum;         // 게시물 ID
    private String title;           // 제목
    private int like;               // 좋아요 수
    private LocalDateTime date;     // 작성일자
}
