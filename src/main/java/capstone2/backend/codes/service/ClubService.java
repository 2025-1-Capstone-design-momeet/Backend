package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.ClubDto;
import capstone2.backend.codes.dto.WaitingListDecisionDto;
import capstone2.backend.codes.dto.WaitingListDto;
import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.entity.ClubMembers;
import capstone2.backend.codes.entity.WaitingList;
import capstone2.backend.codes.repository.ClubMembersRepository;
import capstone2.backend.codes.repository.ClubRepository;
import capstone2.backend.codes.repository.WaitingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final WaitingListRepository waitingListRepository;

    // 내 클럽 조회
    public List<Club> getClubsByUserId(String userId) throws Exception {
        try {
            return clubMembersRepository.findClubsByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public List<Club> getClubsByUnivName(String univName) throws Exception {
        try {
            return clubRepository.findAllByUnivName(univName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    // 동아리 추가
    public boolean addClub(ClubDto clubDto) throws Exception {
        try {
            // 같은 학교에 같은 이름의 동아리가 이미 존재하는지 확인
            boolean exists = clubRepository.existsByClubNameAndUnivName(clubDto.getClubName(), clubDto.getUnivName());
            if (exists) return false;

            Club club = new Club();
            club.setClubId(UUID.randomUUID().toString().replace("-", ""));
            club.setClubName(clubDto.getClubName());
            club.setProfileImage(null);
            club.setManagerId(clubDto.getManagerId());
            club.setCategory(clubDto.getCategory());
            club.setBannerImage(null);
            club.setUnivName(clubDto.getUnivName());
            club.setOfficial(false);
            clubRepository.save(club);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("동아리 추가 중 오류 발생", e);
        }
    }

    // 동아리 삭제
    public boolean applyToClub(WaitingListDto waitingListDto) {
        try {
            // 중복 신청 여부 확인
            boolean exists = waitingListRepository.existsByUserIdAndClubId(
                    waitingListDto.getUserId(),
                    waitingListDto.getClubId()
            );
            if (exists) return false;

            // 신청 정보 저장
            WaitingList waiting = new WaitingList();
            waiting.setUserId(waitingListDto.getUserId());
            waiting.setClubId(waitingListDto.getClubId());
            waiting.setWhy(waitingListDto.getWhy());
            waiting.setWhat(waitingListDto.getWhat());
            waitingListRepository.save(waiting);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean processClubApplication(WaitingListDecisionDto dto) {
        try {
            Optional<WaitingList> optional = waitingListRepository.findByUserIdAndClubId(dto.getUserId(), dto.getClubId());
            if (optional.isEmpty()) {
                return false;
            }

            // 승인 처리
            if ("approve".equalsIgnoreCase(dto.getAction())) {
                ClubMembers member = new ClubMembers();
                member.setUserId(dto.getUserId());
                member.setClubId(dto.getClubId());
                member.setRole(null);
                clubMembersRepository.save(member);
            }

            // 승인이든 거절이든 WaitingList에서 삭제
            waitingListRepository.delete(optional.get());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
