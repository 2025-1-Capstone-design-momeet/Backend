package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.CalendarDto;
import capstone2.backend.codes.dto.ClubIdRequestDto;
import capstone2.backend.codes.dto.ScheduleRequestDto;
import capstone2.backend.codes.dto.UpcomingScheduleDto;
import capstone2.backend.codes.entity.Calendar;
import capstone2.backend.codes.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    // ✅ 1. 클럽 일정 전체 조회
    @PostMapping("/list")
    public ResponseEntity<Response<?>> getCalendar(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            List<Calendar> schedules = calendarService.getCalendarByClubId(clubIdRequestDto.getClubId())    ;
            return ResponseEntity.ok(new Response<>("true", "일정 목록 조회 성공", schedules));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "일정 목록 조회 중 오류 발생", null));
        }
    }

    // ✅ 2. 일정 추가
    @PostMapping
    public ResponseEntity<Response<?>> addSchedule(@RequestBody CalendarDto dto) {
        try {
            calendarService.addSchedule(dto);
            return ResponseEntity.ok(new Response<>("true", "일정 등록 완료", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "일정 등록 중 오류 발생", null));
        }
    }

    // ✅ 3. 일정 삭제
    @PostMapping("/delete")
    public ResponseEntity<Response<?>> deleteSchedule(@RequestBody ScheduleRequestDto scheduleRequestDto) {
        try {
            calendarService.deleteSchedule(scheduleRequestDto.getScheduleId());
            return ResponseEntity.ok(new Response<>("true", "일정 삭제 완료", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "일정 삭제 중 오류 발생", null));
        }
    }

    // ✅ 4. 가장 가까운 일정 조회
    @PostMapping("/upcoming")
    public ResponseEntity<Response<?>> getUpcomingSchedule(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            Optional<UpcomingScheduleDto> upcomingSchedule
                    = calendarService.getUpcomingSchedule(clubIdRequestDto.getClubId());
            return upcomingSchedule.
                    <ResponseEntity<Response<?>>>map(
                            upcomingScheduleDto-> ResponseEntity.ok(new Response<>(
                                    "true", "가장 가까운 일정 조회 성공", upcomingScheduleDto)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>("false", "가장 가까운 일정이 없습니다.", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "가까운 일정 조회 중 오류 발생", null));
        }
    }
}
