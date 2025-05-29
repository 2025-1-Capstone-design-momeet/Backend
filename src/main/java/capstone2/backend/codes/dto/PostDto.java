package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private String postNum;
    private String title;
    private String content;

    private int type;             // DB에 저장된 정수형 타입
    private String typeLabel;     // enum에서 가져온 한글 설명
    private String file;

    private int like;
    private int fixation;
    private LocalDateTime date;
}
