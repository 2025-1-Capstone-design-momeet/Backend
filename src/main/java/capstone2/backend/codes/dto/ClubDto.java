package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {
    private String clubId;
    private String clubName;
    private String profileImage;
    private String managerId;
    private String category;
    private String bannerImage;
    private String univName;
    private boolean isOfficial; // 공식 동아리 여부 (1: 공식, 0: 비공식)
}
