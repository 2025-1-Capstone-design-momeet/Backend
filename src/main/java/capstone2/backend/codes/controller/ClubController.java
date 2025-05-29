package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/club")
public class ClubController {
    private final ClubService clubService;
    @PostMapping("/main")
    public ResponseEntity<Response<?>> getClubMain(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            ClubMainDto dto = clubService.getClubMainInfo(clubIdRequestDto.getClubId());
            return ResponseEntity.ok(new Response<>("true", "클럽 메인 페이지 조회 성공", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>("false", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "클럽 메인 정보 조회 중 오류 발생", null));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Response<?>> createClub(@RequestBody ClubDto clubDto) {
        try {
            boolean result = clubService.addClub(clubDto);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(
                        "true", "동아리 생성 성공", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 생성 실패: 이미 존재하는 동아리입니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 생성 중 오류 발생", null));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<Response<?>> applyToClub(@RequestBody WaitingListDto waitingListDto) {
        try {
            boolean result = clubService.applyToClub(waitingListDto);
            if (result) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(
                        "true", "동아리 신청 성공", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 신청 실패: 이미 신청한 동아리입니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 신청 중 오류 발생", null));
        }
    }

    @PostMapping("/application/decision")
    public ResponseEntity<Response<?>> approveApplication(@RequestBody WaitingListDecisionDto waitingListDecisionDto) {
        try {
            boolean result = clubService.processClubApplication(waitingListDecisionDto);
            if (result) {
                return ResponseEntity.ok(new Response<>(
                        "true", "동아리 신청 승인 성공", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 신청 승인 실패: 해당 신청이 존재하지 않습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 신청 승인 중 오류 발생", null));
        }
    }
}
