package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.UserDto;
import capstone2.backend.codes.dto.UserMainDto;
import capstone2.backend.codes.entity.University;
import capstone2.backend.codes.entity.User;
import capstone2.backend.codes.repository.*;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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
    private final ClubRepository clubRepository;
    private final ClubPromotionRepository clubPromotionRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final PosterRepository posterRepository;

    public User getUser(String userId) throws Exception {
        try {
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional
    public boolean registerUser(UserDto userDto) throws Exception {
        try {
            // 사용자 ID 중복 확인
            if (userRepository.existsById(userDto.getUserId())) {
                return false;
            }
            else {
                User user = new User(
                        userDto.getUserId(),
                        passwordEncoder.encode(userDto.getPw()),
                        passwordEncoder.encode(userDto.getPhoneNum()),
                        userDto.getName(),
                        passwordEncoder.encode(userDto.getEmail()),
                        null,
                        false,
                        null,
                        null,
                        null,
                        userDto.isGender()
                );
                userRepository.save(user);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public boolean updateUser(UserDto userDto) throws Exception {
        try {
            User user = userRepository.findById(userDto.getUserId()).orElse(null);
            if (user == null) return false;
            user = new User(
                    userDto.getUserId(),
                    passwordEncoder.encode(userDto.getPw()),
                    passwordEncoder.encode(userDto.getPhoneNum()),
                    userDto.getName(),
                    passwordEncoder.encode(userDto.getEmail()),
                    passwordEncoder.encode(userDto.getUnivName()),
                    userDto.isSchoolCertification(),
                    passwordEncoder.encode(userDto.getDepartment()),
                    passwordEncoder.encode(userDto.getStudentNum()),
                    userDto.getGrade(),
                    userDto.isGender()
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 로그인 이외에도 비밀번호 확인할 때 사용 가능함
    public boolean loginUser(UserDto userDto) throws Exception {
        try {
            User user = userRepository.findById(userDto.getUserId()).orElse(null);
            if (user == null) return false;
            return passwordEncoder.matches(userDto.getPw(), user.getPw());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    //

    // 학교인증
    public boolean sendVerificationCode(String email) throws Exception {
        try {
            String code = String.format("%06d", new Random().nextInt(1000000));

            redisTemplate.opsForValue().set("verify:" + email, code, 5, TimeUnit.MINUTES);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(email);
            helper.setFrom("no-reply@momeet.meowning.kr", "mo.meet");
            helper.setSubject("[모밋] 학교 이메일 인증 코드");

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            String html = """
                <div style="font-family: Arial, sans-serif; font-size: 15px; padding: 40px; background-color: #f4f4f4; color: #2D3748;">
                    <div style="max-width: 460px; margin: auto; background-color: white; padding: 30px 40px; border-radius: 10px;
                                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);">
                        
                        <div style="text-align: center; font-size: 20px; font-weight: bold; color: #71B271; margin-bottom: 20px;">
                            mo.meet
                        </div>
            
                        <h2 style="text-align: center; color: #2D3748; margin-bottom: 16px;">이메일 인증 코드</h2>
            
                        <p style="text-align: center;">아래 코드를 입력해 주세요.</p>
            
                        <div style="text-align: center; margin: 24px 0;">
                            <div style="display: inline-block; padding: 14px 28px; font-size: 22px; font-weight: bold;
                                        background-color: #F0F4F0; color: #2D3748; border-radius: 8px;
                                        user-select: all; -webkit-user-select: all; cursor: text;">
                                %s
                            </div>
                        </div>
            
                        <p style="text-align: center; font-size: 14px;">
                            이 코드는 <strong>5분간 유효</strong>합니다.
                        </p>
                        <p style="text-align: center; font-size: 13px; color: gray;">
                            인증 요청을 하지 않았다면 이 메일은 무시해 주세요.
                        </p>
                    </div>
                </div>
            """.formatted(code);

            helper.setText(html, true);
            mailSender.send(message);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    @Transactional
    public void setUserUniversity(String userId, String univName) throws Exception{
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }


    public boolean verifyCode(String email, String inputCode) throws Exception {
        try {
            String key = "verify:" + email;
            String savedCode = redisTemplate.opsForValue().get(key);

            if (savedCode.equals(inputCode)) {
                redisTemplate.delete(key); // 인증 성공 시 코드 제거 (선택적)
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // public UserMainDto(User u, List<Club> mc, List<Poster> p, List<ClubPromotion> cp)
    public UserMainDto getUserMainInfo(String userId) throws Exception {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return new UserMainDto(user,
                        clubMembersRepository.findClubsByUserId(userId),
                        posterRepository.findAllByUnivName(user.getUnivName()),
                        clubPromotionRepository.findAllByUnivName(user.getUnivName())
                );
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
}
