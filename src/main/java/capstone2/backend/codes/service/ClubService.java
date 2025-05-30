package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.Club;
import capstone2.backend.codes.entity.ClubMembers;
import capstone2.backend.codes.entity.Post;
import capstone2.backend.codes.entity.WaitingList;
import capstone2.backend.codes.repository.ClubMembersRepository;
import capstone2.backend.codes.repository.ClubPostRepository;
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
    private final ClubPostRepository clubPostRepository;
    private final CalendarService calendarService;

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

    public List<RecentPostDto> getTop3RecentPosts(String clubId) {
        return clubPostRepository.findTop3ByClub_ClubIdOrderByPost_FixactionDescPost_DateDesc(clubId).stream()
                .map(cp -> {
                    Post p = cp.getPost();
                    return new RecentPostDto(
                            p.getPostNum(),
                            p.getTitle(),
                            p.getLike(),
                            p.getDate()
                    );
                })
                .toList();
    }
    public ClubMainDto getClubMainInfo(String clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("동아리가 존재하지 않습니다."));

        // TODO: 여기 president, executive 엔티티, 여기 인원 합산 기능 만들고 club create할 때 managerId를 회장에 추가해야함

        // ✅ 인원 수 계산: 회장 + 임원 + 멤버
        //int presidentCount = presidentRepository.countByClubId(clubId);
        //int executiveCount = executiveRepository.countByClubId(clubId);
        int memberCount = clubMembersRepository.countByClubId(clubId);
        //int totalCount = presidentCount + executiveCount + memberCount;
        int totalCount = memberCount; // 현재는 회장과 임원 수를 고려하지 않음

        String welcomeMessage = "안녕하세요, " + club.getClubName() + " 입니다!";
        String bannerImage = club.getBannerImage();

        // ✅ 가장 가까운 일정 가져오기
        UpcomingScheduleDto upcoming = calendarService.getUpcomingSchedule(clubId).orElse(null);

        // ✅ 최근 게시글 3개 가져오기 (fixaction 우선 → 최신순)
        List<RecentPostDto> recentPosts = clubPostRepository
                .findTop3ByClub_ClubIdOrderByPost_FixactionDescPost_DateDesc(clubId)
                .stream()
                .map(cp -> {
                    Post post = cp.getPost();
                    return new RecentPostDto(
                            post.getPostNum(),
                            post.getTitle(),
                            post.getLike(),
                            post.getDate()
                    );
                })
                .toList();

        return new ClubMainDto(
                club.getClubName(),
                club.getUnivName(),
                club.getCategory(),
                club.isOfficial(),
                totalCount,  // ✅ 총 인원 수 반영
                bannerImage,
                welcomeMessage,
                upcoming != null ? List.of(upcoming) : List.of(),
                recentPosts
        );
    }
}
