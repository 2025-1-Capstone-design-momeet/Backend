package capstone2.backend.codes.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonalNotificationDto {
    private String notificationId;
    private int type;
    private String content;
    private String title;
    private String userId;
    private String payId;
}
