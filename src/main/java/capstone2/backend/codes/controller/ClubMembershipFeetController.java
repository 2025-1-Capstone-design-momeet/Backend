package capstone2.backend.codes.controller;

import capstone2.backend.codes.config.Response;
import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.service.ClubMembershipService;
import capstone2.backend.codes.service.PaymentService;
import capstone2.backend.codes.service.VoteService;
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
@RequestMapping("/api/membershipFee")
public class ClubMembershipFeetController {
    private final VoteService voteService;
    private final ClubMembershipService clubMembershipService;

    @PostMapping("/payMembers")
    public ResponseEntity<Response<?>> payMembers(@RequestBody ClubmembershipFeeInputDto clubmembershipFeeInputDto) {
        try {
            PaymentMembersDto paymentMembersDto = clubMembershipService.payMembers(clubmembershipFeeInputDto);
            return ResponseEntity.ok(new Response<>("true", "가입비 할 인원 가져오기 성공", paymentMembersDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "가입비 할 인원에 실패했습니다.", null));
        }
    }

    @PostMapping("/write")
    public ResponseEntity<Response<?>> writePost(
            @RequestBody PaymentWriteDto paymentWriteDto) {
        try {
            if (!clubMembershipService.writePayment(paymentWriteDto)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "가입비 작성에 실패했습니다.", null));
            } else {
                return ResponseEntity.ok(
                        new Response<>("true", "가입비 작성 성공", null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "가입비 작성에 실패했습니다.", null));
        }
    }

    @PostMapping("/check")
    public ResponseEntity<Response<?>> payCheck(
            @RequestBody PaymentStateDto paymentStateDto) {
        try {
            if (!clubMembershipService.payCheck(paymentStateDto)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>("false", "유저 가입비 정산에 실패했습니다.", null));
            } else {
                return ResponseEntity.ok(
                        new Response<>("true", "유저 가입비 정산에 성공", null)
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "유저 가입비 정산에 실패했습니다.", null));
        }
    }

    @PostMapping("/getPaymentStatesByPayId")
    public ResponseEntity<Response<?>> getPaymentStatesByPayId(@RequestBody PaymentStateDto paymentStateDto) {
        try {
            List<PaymentStateDto> paymentStates = clubMembershipService.getPaymentStatesByPayId(paymentStateDto);
            return ResponseEntity.ok(new Response<>("true", "특정 가입비에 대한 가입비 현황 리스트 가져오기 성공", paymentStates));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "특정 가입비에 대한 가입비 현황 리스트 에 실패했습니다.", null));
        }
    }

    @PostMapping("/getPaymentList")
    public ResponseEntity<Response<?>> getPaymentList(@RequestBody PaymentHistoryListIdDto paymentHistoryListIdDto) {
        try {
            PaymentHistoryDto paymentHistory = clubMembershipService.getPaymentList(paymentHistoryListIdDto);
            if (paymentHistory == null) {
                paymentHistory = new PaymentHistoryDto(); // 빈 생성자 + 내부 리스트 빈 상태로
            }
            return ResponseEntity.ok(new Response<>("true", "정산 가입비 가져오기 성공", paymentHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "정산 가입비 가져오기에 실패했습니다.", null));
        }
    }

    @PostMapping("/getManagementPaymentList")
    public ResponseEntity<Response<?>> getManagementPaymentList(@RequestBody PaymentHistoryListIdDto paymentHistoryListIdDto) {
        try {
            PaymentHistoryDto paymentHistory = clubMembershipService.getManagementPaymentList(paymentHistoryListIdDto);
            if (paymentHistory == null) {
                paymentHistory = new PaymentHistoryDto(); // 빈 생성자 + 내부 리스트 빈 상태로
            }
            return ResponseEntity.ok(new Response<>("true", "관리자 가입비 리스트 가져오기 성공", paymentHistory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("false", "관리자 가입비 리스트 가져오기에 실패했습니다.", null));
        }
    }
}