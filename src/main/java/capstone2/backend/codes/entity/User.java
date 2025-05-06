package capstone2.backend.codes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "User")
public class User {
    @Id
    @Column(name = "userId", nullable = false)
    private String userId;
    @Column(name = "password", nullable = false)
    private String pw;
    @Column(name = "phoneNum", nullable = false)
    private String phoneNum;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "univName")
    private String univName;
    @Column(name = "schoolCertification", nullable = false)
    private boolean schoolCertification;
    @Column(name = "department")
    private String department;
    @Column(name = "studentNum")
    private String studentNum;
    @Column(name = "grade")
    private String grade;
    @Column(name = "gender", nullable = false)
    private boolean gender;
}