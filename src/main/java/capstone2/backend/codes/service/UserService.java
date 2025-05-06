package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.UserDto;
import capstone2.backend.codes.entity.University;
import capstone2.backend.codes.entity.User;
import capstone2.backend.codes.repository.UniversityRepository;
import capstone2.backend.codes.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final UniversityRepository universityRepository;

    public User getUser(String userId) { return userRepository.findById(userId).orElse(null); }
    public User registerUser(UserDto userDto) {
        User user = new User(
                userDto.getUserId(),
                passwordEncoder.encode(userDto.getPw()),
                passwordEncoder.encode(userDto.getPhoneNum()),
                userDto.getName(),
                userDto.getEmail(),
                null,
                false,
                null,
                null,
                null,
                userDto.isGender()
        );
        user = userRepository.save(user);

        // 민감 정보 제거
        user.setPw(null);
        user.setPhoneNum(null);
        user.setStudentNum(null);

        return user;
    }

    public User updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getUserId()).orElse(null);
        if (user == null) return null;
        user = new User(
                userDto.getUserId(),
                passwordEncoder.encode(userDto.getPw()),
                passwordEncoder.encode(userDto.getPhoneNum()),
                userDto.getName(),
                userDto.getEmail(),
                userDto.getUnivName(),
                userDto.isSchoolCertification(),
                userDto.getDepartment(),
                passwordEncoder.encode(userDto.getStudentNum()),
                userDto.getGrade(),
                userDto.isGender()
        );
        return userRepository.save(user);
    }

    // 로그인 이외에도 비밀번호 확인할 때 사용 가능함
    public boolean loginUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getUserId()).orElse(null);
        if (user == null) return false;
        return passwordEncoder.matches(userDto.getPw(), user.getPw());
    }

    // 학교인증
    public boolean sendVerificationCode(String email) {
        try {
            // 1. 인증 코드 생성
            String code = String.format("%06d", new Random().nextInt(1000000));

            // 2. Redis에 5분간 저장
            redisTemplate.opsForValue().set("verify:" + email, code, 5, TimeUnit.MINUTES);

            // 3. HTML 메일 발송
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(email);
            helper.setFrom("no-reply@momeet.meowning.kr");
            helper.setSubject("[모밋] 학교 이메일 인증 코드");

            String html = """
                <div style="font-family: Arial, sans-serif; font-size: 14px; padding: 20px;">
                    <h2 style="color: #5A67D8;">모밋 이메일 인증</h2>
                    <p>안녕하세요!</p>
                    <p>요청하신 인증 코드는 다음과 같습니다:</p>
                    <div style="font-size: 24px; font-weight: bold; color: #2D3748; margin: 20px 0;">%s</div>
                    <p>이 코드는 <strong>5분간 유효</strong>합니다.</p>
                    <p style="color: gray;">실수로 받으셨다면 무시하셔도 됩니다.</p>
                </div>
            """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public void setUserUniversity(String userId, String univName) {
        // 1. 학교 DB에 등록되어 있지 않으면 추가
        if (!universityRepository.existsById(univName)) {
            universityRepository.save(new University(univName));
        }

        // 2. 사용자 정보 업데이트
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setUnivName(univName);
            user.setSchoolCertification(true); // 인증 완료
            userRepository.save(user);
        }
    }


    public boolean verifyCode(String email, String inputCode) {
        String key = "verify:" + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(key); // 인증 성공 시 코드 제거 (선택적)
            return true;
        }
        return false;
    }
}
