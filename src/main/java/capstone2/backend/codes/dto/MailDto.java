package capstone2.backend.codes.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailDto {
    private String userId;
    private String email;
    private String code;
}
