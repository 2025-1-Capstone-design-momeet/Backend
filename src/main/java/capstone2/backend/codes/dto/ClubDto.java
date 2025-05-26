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
    private int category;
    private String bannerImage;
    private String univName;
}
