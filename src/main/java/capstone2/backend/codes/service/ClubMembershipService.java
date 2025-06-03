package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubMembershipService {
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipFeeHistoryRepository clubMembershipFeeHistoryRepository;
    private final ClubMembershipFeeStateRepository clubMembershipFeeStateRepository;
    private final ClubService clubService;

    // 가입비 받을 인원 가져오기
    public PaymentMembersDto payMembers(ClubmembershipFeeInputDto clubmembershipFeeInputDto) throws Exception{
        try {
            // 관리자 권한 확인
            if (!clubService.canManageClub(clubmembershipFeeInputDto.getUserId(), clubmembershipFeeInputDto.getClubId())) {
                throw new IllegalAccessException("해당 사용자는 클럽 관리자 권한이 없습니다.");
            }
            clubService.getAllUsersInClub(clubmembershipFeeInputDto.getClubId());
            Club club = clubRepository.findById(clubmembershipFeeInputDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            return new PaymentMembersDto(
                    clubService.getAllUsersInClub(clubmembershipFeeInputDto.getClubId())
                            .stream()
                            .map(user -> new UserDto(
                                    user.getUserId(),
                                    user.getPw(),
                                    user.getPhoneNum(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getUnivName(),
                                    user.isSchoolCertification(),
                                    user.getDepartment(),
                                    user.getStudentNum(),
                                    user.getGrade(),
                                    user.isGender()
                            ))
                            .collect(Collectors.toList()),
                    null
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    public Boolean writePayment(PaymentWriteDto paymentWriteDto) throws Exception {
        try {
            Club club = clubRepository.findById(paymentWriteDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            // 1. 해당 동아리 가입비가 이미 있는지 확인 (제목이 "가입비"인 것)
            Optional<ClubMembershipFeeHistory> existingPaymentOpt = clubMembershipFeeHistoryRepository
                    .findByClubIdAndTitle(paymentWriteDto.getClubId(), "가입비");

            ClubMembershipFeeHistory paymentHistory;

            if (existingPaymentOpt.isPresent()) {
                // 2-1. 기존 가입비 업데이트
                paymentHistory = existingPaymentOpt.get();
                paymentHistory.setAmount(paymentWriteDto.getAmount());
                paymentHistory.setAccount(paymentWriteDto.getAccount());
                clubMembershipFeeHistoryRepository.save(paymentHistory);

            } else {
                // 2-2. 신규 가입비 생성
                String payId = UUID.randomUUID().toString().replace("-", "");
                paymentHistory = new ClubMembershipFeeHistory(
                        payId,
                        paymentWriteDto.getClubId(),
                        "가입비",
                        paymentWriteDto.getAmount(),
                        paymentWriteDto.getAccount(),
                        club
                );
                clubMembershipFeeHistoryRepository.save(paymentHistory);
            }

            // 3. 멤버별 정산 상태 업데이트 or 새로 생성
            for (String userId : paymentWriteDto.getPaymentMembers()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));

                Optional<ClubMembershipFeeState> existingStateOpt =
                        clubMembershipFeeStateRepository.findByMembershipFeeIdAndUserId(paymentHistory.getMembershipFeeId(), userId);

                if (existingStateOpt.isPresent()) {
                    // 기존 상태 업데이트 (예: 미납 상태로 변경)
                    ClubMembershipFeeState existingState = existingStateOpt.get();
                    existingState.setHasPaid(false);  // 필요시 값 변경
                    clubMembershipFeeStateRepository.save(existingState);
                } else {
                    // 신규 상태 생성
                    ClubMembershipFeeState paymentState = new ClubMembershipFeeState(
                            paymentHistory.getMembershipFeeId(),
                            userId,
                            false,
                            paymentHistory,
                            user
                    );
                    clubMembershipFeeStateRepository.save(paymentState);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 유저 가입비 정보 바꾸기
    public boolean payCheck(PaymentStateDto paymentStateDto) throws Exception {
        try {
            ClubMembershipFeeHistory pay = clubMembershipFeeHistoryRepository.findById(paymentStateDto.getPayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));

            User user = userRepository.findById(paymentStateDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            ClubMembershipFeeState paymentState = clubMembershipFeeStateRepository.findByMembershipFeeIdAndUserId(
                    paymentStateDto.getPayId(),
                    paymentStateDto.getUserId()
            ).orElseThrow(() -> new IllegalArgumentException("해당 유저의 정산 정보를 찾을 수 없습니다."));

            // 상태 변경
            paymentState.setHasPaid(paymentStateDto.isHasPaid());  // 또는 false로 변경도 가능
            clubMembershipFeeStateRepository.save(paymentState);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 특정 가입비에 대한 정산 현황 리스트 조회
    public List<PaymentStateDto> getPaymentStatesByPayId(PaymentStateDto paymentStateDto) throws Exception {
        try {
            // 정산 ID 유효성 체크
            ClubMembershipFeeHistory pay = clubMembershipFeeHistoryRepository.findById(paymentStateDto.getPayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 payId의 정산이 존재하지 않습니다."));

            // 관리자 권한 확인
            if (!clubService.canManageClub(paymentStateDto.getUserId(), pay.getClubId())) {
                throw new IllegalAccessException("해당 사용자는 클럽 관리자 권한이 없습니다.");
            }

            List<ClubMembershipFeeState> uncompletePayEntities = clubMembershipFeeStateRepository.findByMembershipFeeId(paymentStateDto.getPayId());

            List<PaymentStateDto> completePay = uncompletePayEntities.stream()
                    .map(entity -> new PaymentStateDto(entity.getMembershipFeeId(), entity.getUserId(), entity.getUser().getName(),entity.isHasPaid()))
                    .collect(Collectors.toList());

            return completePay;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 가입비 리스트 가져오기
    public PaymentHistoryDto getPaymentList(PaymentHistoryListIdDto paymentHistoryListIdDto) throws Exception{
        try {
            userRepository.findById(paymentHistoryListIdDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            clubRepository.findById(paymentHistoryListIdDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            ClubMembershipFeeHistory completePayEntities = clubMembershipFeeStateRepository.findPaidPaymentsByUserInClub(
                    paymentHistoryListIdDto.getClubId(),
                    paymentHistoryListIdDto.getUserId()
            );

            ClubMembershipFeeHistory uncompletePayEntities = clubMembershipFeeStateRepository.findUnpaidPaymentsByUserInClub(
                    paymentHistoryListIdDto.getClubId(),
                    paymentHistoryListIdDto.getUserId()
            );

            PaymentHistoryDto completePay = convertToDto(completePayEntities);
            PaymentHistoryDto uncompletePay = convertToDto(uncompletePayEntities);
            if (completePayEntities == null){
                return uncompletePay ;
            }
            else if (uncompletePayEntities ==null){
                completePay.setComplete(true);
                return completePay;
            }
            return new PaymentHistoryDto(null,
                    paymentHistoryListIdDto.getClubId(),
                    "가입비",
                    0,
                    null,
                    false
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 관리자 가입비 리스트 가져오기
    public PaymentHistoryDto getManagementPaymentList(PaymentHistoryListIdDto paymentHistoryListIdDto) throws Exception{
        try {
            // 관리자 권한 확인
            if (!clubService.canManageClub(paymentHistoryListIdDto.getUserId(), paymentHistoryListIdDto.getClubId())) {
                throw new IllegalAccessException("해당 사용자는 클럽 관리자 권한이 없습니다.");
            }
            userRepository.findById(paymentHistoryListIdDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            clubRepository.findById(paymentHistoryListIdDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            ClubMembershipFeeHistory completePayEntities = clubMembershipFeeStateRepository.findFullyPaidHistoriesByClubId(
                    paymentHistoryListIdDto.getClubId()
            );

            ClubMembershipFeeHistory uncompletePayEntities = clubMembershipFeeStateRepository.findUnpaidByAllMembersInClub(
                    paymentHistoryListIdDto.getClubId()
            );

            PaymentHistoryDto completePay = convertToDto(completePayEntities);
            PaymentHistoryDto uncompletePay = convertToDto(uncompletePayEntities);
            if (completePayEntities == null){
                return uncompletePay ;
            }
            else if (uncompletePayEntities ==null){
                completePay.setComplete(true);
                return completePay;
            }
            return new PaymentHistoryDto(null,
                    paymentHistoryListIdDto.getClubId(),
                    "가입비",
                    0,
                    null,
                    false
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    public PaymentHistoryDto convertToDto(ClubMembershipFeeHistory entity) {
        if (entity == null) return null;
        return new PaymentHistoryDto(
                entity.getMembershipFeeId(),
                entity.getClubId(),
                entity.getTitle(),
                entity.getAmount(),
                entity.getAccount(),
                false
        );
    }
}
