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
public class PaymentService {
    private final VoteRepository voteRepository;
    private final VoteContentRepository voteContentRepository;
    private final VoteStateRepository voteStateRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentStateRepository paymentStateRepository;
    private final ClubService clubService;

    // 투표에서 정산 할 인원 가져오기
    public PaymentMembersDto payMembers(VoteStateDto voteStateDto) throws Exception{
        try {
            Vote vote = voteRepository.findById(voteStateDto.getVoteID())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteID의 투표를 찾을 수 없습니다."));

            // 관리자 권한 확인
            if (!clubService.canManageClub(voteStateDto.getUserId(), vote.getClubId())) {
                throw new IllegalAccessException("해당 사용자는 클럽 관리자 권한이 없습니다.");
            }
            Club club = clubRepository.findById(vote.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));
            VoteContent voteContent = voteContentRepository.findById(voteStateDto.getVoteContentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteContentId의 투표 번호를 찾을 수 없습니다."));

            return new PaymentMembersDto(
                    voteContentRepository.findUsersByVoteContentAndContentId(vote.getVoteID(),voteStateDto.getVoteContentId()),
                    voteContentRepository.findUsersNotSelectedVoteContent(club.getClubId(),voteStateDto.getVoteContentId())
            );
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 정산 작성하기
    public Boolean writePayment(PaymentWriteDto paymentWriteDto) throws Exception {
        try {
            String payId = UUID.randomUUID().toString().replace("-", "");
            Vote vote = voteRepository.findById(paymentWriteDto.getVoteID())
                    .orElseThrow(() -> new IllegalArgumentException("해당 voteID의 투표를 찾을 수 없습니다."));
            Club club = clubRepository.findById(paymentWriteDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            vote.setPayed(true);
            voteRepository.save(vote);

            PaymentHistory paymentHistory = new PaymentHistory(
                    payId,
                    paymentWriteDto.getClubId(),
                    paymentWriteDto.getTitle(),
                    paymentWriteDto.getAmount(),
                    paymentWriteDto.getAccount(),
                    club
            );
            paymentHistoryRepository.save(paymentHistory);

            for(String userId: paymentWriteDto.getPaymentMembers()){
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
                PaymentState paymentState = new PaymentState(
                        payId,
                        userId,
                        false,
                        paymentHistory,
                        user
                );
                paymentStateRepository.save(paymentState);
            }

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 유저 정산 정보 바꾸기
    public boolean payCheck(PaymentStateDto paymentStateDto) throws Exception {
        try {
            PaymentHistory pay = paymentHistoryRepository.findById(paymentStateDto.getPayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));

            // 관리자 권한 확인
            if(clubService.canManageClub(paymentStateDto.getUserId(),pay.getClubId())){
                return false;
            }
            User user = userRepository.findById(paymentStateDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            PaymentState paymentState = paymentStateRepository.findByPayIdAndUserId(
                    paymentStateDto.getPayId(),
                    paymentStateDto.getUserId()
            ).orElseThrow(() -> new IllegalArgumentException("해당 유저의 정산 정보를 찾을 수 없습니다."));

            // 상태 변경
            paymentState.setHasPaid(paymentStateDto.isHasPaid());  // 또는 false로 변경도 가능
            paymentStateRepository.save(paymentState);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 특정 정산에 대한 정산 현황 리스트 조회
    public List<PaymentStateDto> getPaymentStatesByPayId(PaymentStateDto paymentStateDto) throws Exception {
        try {
            // 정산 ID 유효성 체크
            PaymentHistory pay = paymentHistoryRepository.findById(paymentStateDto.getPayId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 payId의 정산이 존재하지 않습니다."));

            // 관리자 권한 확인
            if (!clubService.canManageClub(paymentStateDto.getUserId(), pay.getClubId())) {
                throw new IllegalAccessException("해당 사용자는 클럽 관리자 권한이 없습니다.");
            }

            List<PaymentState> uncompletePayEntities = paymentStateRepository.findByPayId(paymentStateDto.getPayId());

            List<PaymentStateDto> completePay = uncompletePayEntities.stream()
                    .map(entity -> new PaymentStateDto(entity.getPayId(), entity.getUserId(), entity.getUser().getName(),entity.isHasPaid()))
                    .collect(Collectors.toList());

            return completePay;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 정산 리스트 가져오기
    public PaymentHistoryListDto getPaymentList(PaymentHistoryListIdDto paymentHistoryListIdDto) throws Exception{
        try {
            userRepository.findById(paymentHistoryListIdDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            clubRepository.findById(paymentHistoryListIdDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            List<PaymentHistory> completePayEntities = paymentStateRepository.findPaidPaymentsByUserInClub(
                    paymentHistoryListIdDto.getClubId(),
                    paymentHistoryListIdDto.getUserId()
            );

            List<PaymentHistory> uncompletePayEntities = paymentStateRepository.findUnpaidPaymentsByUserInClub(
                    paymentHistoryListIdDto.getClubId(),
                    paymentHistoryListIdDto.getUserId()
            );

            List<PaymentHistoryDto> completePay = convertToDtoList(completePayEntities);
            List<PaymentHistoryDto> uncompletePay = convertToDtoList(uncompletePayEntities);

            return new PaymentHistoryListDto(
                    completePay,uncompletePay);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 관리자 정산 리스트 가져오기
    public PaymentHistoryListDto getManagementPaymentList(PaymentHistoryListIdDto paymentHistoryListIdDto) throws Exception{
        try {
            // 관리자 권한 확인
            if (!clubService.canManageClub(paymentHistoryListIdDto.getUserId(), paymentHistoryListIdDto.getClubId())) {
                throw new IllegalAccessException("해당 사용자는 클럽 관리자 권한이 없습니다.");
            }
            userRepository.findById(paymentHistoryListIdDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저를 찾을 수 없습니다."));
            clubRepository.findById(paymentHistoryListIdDto.getClubId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 clubId의 동아리를 찾을 수 없습니다."));

            List<PaymentHistory> completePayEntities = paymentStateRepository.findFullyPaidHistoriesByClubId(
                    paymentHistoryListIdDto.getClubId()
            );

            List<PaymentHistory> uncompletePayEntities = paymentStateRepository.findUnpaidByAllMembersInClub(
                    paymentHistoryListIdDto.getClubId()
            );

            List<PaymentHistoryDto> completePay = convertToDtoList(completePayEntities);
            List<PaymentHistoryDto> uncompletePay = convertToDtoList(uncompletePayEntities);

            return new PaymentHistoryListDto(
                    completePay,uncompletePay);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    public List<PaymentHistoryDto> convertToDtoList(List<PaymentHistory> entities) {
        return entities.stream()
                .map(p -> new PaymentHistoryDto(
                        p.getPayId(),
                        p.getClubId(),
                        p.getTitle(),
                        p.getAmount(),
                        p.getAccount()
                ))
                .collect(Collectors.toList());
    }

}