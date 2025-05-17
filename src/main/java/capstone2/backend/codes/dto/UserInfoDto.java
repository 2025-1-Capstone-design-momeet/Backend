package capstone2.backend.codes.dto;

import capstone2.backend.codes.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String userId;
    private String name;
    private String univName;
    private boolean schoolCertification;
    private String department;
    private String grade;

    public UserInfoDto(User u) {
        if (u != null) {
            this.userId = u.getUserId();
            this.name = u.getName();
            this.univName = u.getUnivName();
            this.schoolCertification = u.isSchoolCertification();
            this.department = u.getDepartment();
            this.grade = u.getGrade();
        }
        else {
            this.userId = null;
            this.name = null;
            this.univName = null;
            this.schoolCertification = false;
            this.department = null;
            this.grade = null;
        }
    }
}
