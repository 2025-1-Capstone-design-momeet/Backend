package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.MailDto;
import capstone2.backend.codes.service.UserService;
import capstone2.backend.codes.util.UnivDomainMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/univ")
public class UnivMatchController {

    private final UnivDomainMatcher matcher;
    private final UserService userService;

    @PostMapping("/match")
    public ResponseEntity<Response<?>> matchUniv(@RequestBody MailDto dto) {
        String email = dto.getEmail();
        String code = dto.getCode();
        String userId = dto.getUserId();

        // [1] 이메일이 없는 경우
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("false", "이메일이 필요합니다.", null));
        }

        // [2] 학교 도메인 매칭
        String univName = matcher.match(email);
        if (univName == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>("false", "학교 이메일을 인식할 수 없습니다.", null));
        }

        // [3] 인증 코드가 없는 경우 → 인증 메일 발송
        if (code == null || code.isBlank()) {
            boolean sent = userService.sendVerificationCode(email);
            if (!sent) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new Response<>("false", "인증 코드 전송에 실패했습니다.", null));
            }
            return ResponseEntity.ok(new Response<>("true", "인증 코드가 이메일로 전송되었습니다.", null));
        }

        // [4] 인증 코드가 존재하는 경우 → userId도 필수
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("false", "userId가 필요합니다.", null));
        }

        // [5] 인증 코드 검증
        boolean isVerified = userService.verifyCode(email, code);
        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("false", "인증 코드가 일치하지 않습니다.", null));
        }

        // [6] DB에 학교 정보 반영
        userService.setUserUniversity(userId, univName);
        return ResponseEntity.ok(new Response<>("true", "학교 인증이 완료되었습니다.", Map.of("univName", univName)));
    }
}
