package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.service.ClubService;
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
            Club club = clubService.addClub(clubDto);
            if (club != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(
                        "true", "동아리 생성 성공", club));
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
            ClubIdRequestDto clubIdDto = clubService.applyToClub(waitingListDto);
            if (clubIdDto != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(
                        "true", "동아리 신청 성공", clubIdDto));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 신청 실패: 이미 신청한 동아리입니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 신청 중 오류 발생", null));
        }
    }

    @PostMapping("/promotion/write")
    public ResponseEntity<Response<?>> writeClubPromotion(@RequestBody ClubPromotionDto clubPromotionDto) {
        try {
            ClubPromotionDto resultDto = clubService.writeClubPromotion(clubPromotionDto);
            System.out.println("DEBUG >>> clubPromotionDto class = " + clubPromotionDto.getClass().getName());

            if (resultDto != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(
                        "true", "동아리 홍보 작성 성공", resultDto));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 홍보 작성 실패: 해당 동아리가 존재하지 않습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 홍보 작성 중 오류 발생", null));
        }
    }

    @PostMapping("/promotion/detail")
    public ResponseEntity<Response<?>> getClubPromotionDetail(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            ClubPromotionDetailDto promotionDto = clubService.getClubPromotionDetail(clubIdRequestDto);
            return ResponseEntity.ok(new Response<>(
                    "true", "동아리 홍보 상세 조회 성공", promotionDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 홍보 상세 조회 중 오류 발생", null));
        }
    }

    @PostMapping("/application/decision")
    public ResponseEntity<Response<?>> approveApplication(@RequestBody WaitingListDecisionDto waitingListDecisionDto) {
        try {
            ClubIdRequestDto clubIdDto = clubService.processClubApplication(waitingListDecisionDto);
            if (clubIdDto != null) {
                return ResponseEntity.ok(new Response<>(
                        "true", "동아리 신청 승인/거절 성공", clubIdDto));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 신청 승인 실패: 해당 신청이 존재하지 않습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 신청 승인 중 오류 발생", null));
        }
    }

    @PostMapping("/application/list")
    public ResponseEntity<Response<?>> getClubApplicationList(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            List<WaitingListListDto> waitingList = clubService.getClubApplicationList(clubIdRequestDto);
            return ResponseEntity.ok(new Response<>(
                    "true", "동아리 신청 리스트 조회 성공", waitingList));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(
                    "false", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 신청 리스트 조회 중 오류 발생", null));
        }
    }

    @PostMapping("/delegate")
    public ResponseEntity<Response<?>> delegateClub(@RequestBody ClubDelegateDto clubDelegateDto) {
        try {
            ClubDelegateDto resultDto = clubService.delegatePresident(clubDelegateDto);
            if (resultDto != null) {
                return ResponseEntity.ok(new Response<>(
                        "true", "동아리 위임 성공", resultDto));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 위임 실패: 해당 동아리가 존재하지 않거나 권한이 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false","동아리 위임 중 오류 발생", null));
        }
    }

    @PostMapping("/executive/add")
    public ResponseEntity<Response<?>> addExecutive(@RequestBody ExecutiveRequestDto clubExecutiveDto) {
        try {
            ExecutiveRequestDto resultDto = clubService.addClubExecutive(clubExecutiveDto);
            if (resultDto != null) {
                return ResponseEntity.ok(new Response<>(
                        "true", "동아리 임원 추가 성공", resultDto));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 임원 추가 실패: 해당 동아리가 존재하지 않거나 권한이 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 임원 추가 중 오류 발생", null));
        }
    }

    @PostMapping("/executive/remove")
    public ResponseEntity<Response<?>> removeExecutive(@RequestBody ExecutiveRequestDto clubExecutiveDto) {
        try {
            boolean isRemoved = clubService.deleteExecutive(clubExecutiveDto);
            if (isRemoved) {
                return ResponseEntity.ok(new Response<>(
                        "true", "동아리 임원 삭제 성공", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(
                        "false", "동아리 임원 삭제 실패: 해당 동아리가 존재하지 않거나 권한이 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 임원 삭제 중 오류 발생", null));
        }
    }


    @PostMapping("/members")
    public ResponseEntity<Response<?>> getClubMembers(@RequestBody ClubIdRequestDto clubIdRequestDto) {
        try {
            List<ClubMembersDto> members = clubService.getClubMembers(clubIdRequestDto.getClubId());
            return ResponseEntity.ok(new Response<>(
                    "true", "동아리 멤버 리스트 조회 성공", members));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(
                    "false", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 멤버 리스트 조회 중 오류 발생", null));
        }
    }
      
    @PostMapping("/hasPermission")
    public ResponseEntity<Response<?>> hasPermission(@RequestBody ClubUserRequestDto clubUserRequestDto) {
        try {
            boolean hasPermission = clubService.canManageClub(clubUserRequestDto.getClubId(), clubUserRequestDto.getUserId());
            return ResponseEntity.ok(new Response<>(
                    "true", "동아리 권한 확인 성공", hasPermission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(
                    "false", "동아리 권한 확인 중 오류 발생", null));
        }
    }
}
