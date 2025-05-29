package capstone2.backend.codes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubMainDto {
    private String clubName;
    private String univName;
    private String category;
    private boolean isOfficial;
    private int memberCount;

    private String bannerImage;
    private String welcomeMessage;

    private List<UpcomingScheduleDto> upcomingSchedules;
    private List<RecentPostDto> recentPosts;
}
