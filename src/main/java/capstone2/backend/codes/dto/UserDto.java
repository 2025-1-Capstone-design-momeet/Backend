package capstone2.backend.codes.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import  lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String userId;
    private String pw;
    private String phoneNum;
    private String name;
    private String email;
    private String univName;
    private boolean schoolCertification;
    private String department;
    private String studentNum;
    private String grade;
    private boolean gender;
}
