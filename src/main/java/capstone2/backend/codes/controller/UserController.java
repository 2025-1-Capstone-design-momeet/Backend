package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.UserDto;
import capstone2.backend.codes.dto.UserInfoDto;
import capstone2.backend.codes.dto.UserMainDto;
import capstone2.backend.codes.entity.User;
import capstone2.backend.codes.service.ClubService;
import capstone2.backend.codes.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> registerUser(@RequestBody UserDto dto) {
        try {

            if (!userService.registerUser(dto)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "이미 존재하는 사용자 ID입니다.", null));
            }
            else {
                return ResponseEntity.ok(
                        new Response<>("true", "회원가입이 완료되었습니다.", null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "회원가입에 실패했습니다.", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Response<?>> login(@RequestBody UserDto dto) {
        try {
            if (userService.loginUser(dto)) {
                UserInfoDto userInfo = new UserInfoDto(userService.getUser(dto.getUserId()));
                return ResponseEntity.ok(
                        new Response<>("true", userInfo.getName() + "님, 환영합니다.", userInfo)
                );
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new Response<>("false", "아이디 또는 비밀번호가 일치하지 않습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "로그인 중 오류가 발생했습니다.", null));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Response<?>> updateUser(@RequestBody UserDto dto) {
        try {
            if (userService.updateUser(dto)) {
                return ResponseEntity.ok(
                        new Response<>("true", "회원정보가 수정되었습니다.", null)
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("false", "해당 사용자를 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "회원정보 수정 중 오류가 발생했습니다.", null));
        }
    }

    @PostMapping("/info")
    public ResponseEntity<Response<?>> getUserInfo(@RequestBody UserDto dto) {
        try {
            UserInfoDto userInfo = new UserInfoDto(userService.getUser(dto.getUserId()));

            if (userInfo.getUserId() != null) {
                return ResponseEntity.ok(
                        new Response<>("true", "회원정보 조회가 완료되었습니다.", userInfo)
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("false", "해당 사용자를 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "회원정보 조회 중 오류가 발생했습니다.", null));
        }
    }

    @PostMapping("/main")
    public ResponseEntity<Response<?>> getMain(@RequestBody UserDto dto) {
        try {
            UserMainDto main = userService.getUserMainInfo(dto.getUserId());
            if (main.getUserId() != null) {
                return ResponseEntity.ok(
                        new Response<>("true", "메인 페이지 조회가 완료되었습니다.", main)
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("false", "해당 사용자를 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "메인 페이지 조회 중 오류가 발생했습니다.", null));
        }
    }
}
