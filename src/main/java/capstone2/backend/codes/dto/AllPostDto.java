package capstone2.backend.codes.dto;

import capstone2.backend.codes.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AllPostDto {
    private String postNum;
    private String title;
    private String content;
    private int type;                 // DB 저장용
    private String clubName;
    private String univName;
    private String image;
    private LocalDateTime date;

    public AllPostDto(String postNum, String title, String content, int type,
                      String clubName, String univName, String image, LocalDateTime date) {
        this.postNum = postNum;
        this.title = title;
        this.content = content;
        this.type = type;
        this.clubName = clubName;
        this.univName = univName;
        this.image = image;
        this.date = date;
    }

    // 변환용 getter
    public String getTypeLabel() {
        return PostType.fromCode(type).getLabel();
    }
}
