package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.PersonalNotificationDto;
import capstone2.backend.codes.dto.PostDto;
import capstone2.backend.codes.dto.PostNumRequestDto;
import capstone2.backend.codes.dto.UserIdRequestDto;
import capstone2.backend.codes.entity.PersonalNotification;
import capstone2.backend.codes.service.ClubService;
import capstone2.backend.codes.service.PersonalNotificationService;
import capstone2.backend.codes.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alarm")
public class PersonalNotificationController {
    private final PersonalNotificationService personalNotificationService;

    @PostMapping("/getList")
    public ResponseEntity<Response<?>> getList(@RequestBody UserIdRequestDto userIdRequestDto) {
        try {
            List<PersonalNotificationDto> alarmList = personalNotificationService.getAlarmList(userIdRequestDto);
            return ResponseEntity.ok(new Response<>("true", "알림 리스트 가져오기 성공", alarmList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "알림 리스트 가져오기에 실패했습니다.", null));
        }
    }
}
