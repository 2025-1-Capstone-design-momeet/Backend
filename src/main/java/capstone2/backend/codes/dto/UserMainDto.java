package capstone2.backend.codes.dto;

import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.entity.ClubPromotion;
import capstone2.backend.codes.entity.Poster;
import capstone2.backend.codes.entity.User;
import capstone2.backend.codes.service.ClubService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMainDto {
    private String userId;
    private String name;
    private String univName;
    private boolean schoolCertification;
    private String department;
    private String studentNum;
    private String grade;
    private boolean gender;
    private List<ClubSummaryDto> myClubs;
    private List<Poster> posters;
    private List<ClubRecruitmentDto> clubPromotions;

    public UserMainDto(User u, List<ClubSummaryDto> mc, List<Poster> p, List<ClubRecruitmentDto> cp) {
        this.userId = u.getUserId();
        this.name = u.getName();
        this.univName = u.getUnivName();
        this.schoolCertification = u.isSchoolCertification();
        this.department = u.getDepartment();
        this.studentNum = u.getStudentNum();
        this.grade = u.getGrade();
        this.gender = u.isGender();
        this.myClubs = mc;
        this.posters = p;
        this.clubPromotions = cp;
    }
}
