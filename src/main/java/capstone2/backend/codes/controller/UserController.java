package capstone2.backend.codes.controller;

import capstone2.backend.codes.entity.User;
import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.UserDto;
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
        if (userService.getUser(dto.getUserId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Response<>("false", "이미 존재하는 사용자 ID입니다.", null));
        }
        return ResponseEntity.ok(
                new Response<>("true", "회원가입이 완료되었습니다.", userService.registerUser(dto))
        );
    }
}
