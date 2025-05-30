package capstone2.backend.codes.service;

import capstone2.backend.codes.dto.*;
import capstone2.backend.codes.entity.*;
import capstone2.backend.codes.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final WaitingListRepository waitingListRepository;
    private final ClubPostRepository clubPostRepository;
    private final CalendarService calendarService;
    private final ExecutiveRepository executiveRepository;
    private final PresidentRepository presidentRepository;
    private final UserRepository userRepository;

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

    public Club addClub(ClubDto clubDto) throws Exception {
        try {
            // 1. 중복 체크
            boolean exists = clubRepository.existsByClubNameAndUnivName(
                    clubDto.getClubName(), clubDto.getUnivName()
            );
            if (exists) return null;

            // 2. Club 생성
            String clubId = UUID.randomUUID().toString().replace("-", "");

            Club club = new Club();
            club.setClubId(clubId);
            club.setClubName(clubDto.getClubName());
            club.setProfileImage(null);
            club.setManagerId(clubDto.getManagerId());
            club.setCategory(clubDto.getCategory());
            club.setBannerImage(null);
            club.setUnivName(clubDto.getUnivName());
            club.setOfficial(false);

            clubRepository.save(club);

            // 3. 회장 등록
            registerInitialPresident(clubId, clubDto.getManagerId());

            // ✅ 4. Club 반환
            return club;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("동아리 추가 중 오류 발생", e);
        }
    }

    public ClubIdRequestDto applyToClub(WaitingListDto waitingListDto) {
        try {
            // 중복 신청 여부 확인
            boolean exists = waitingListRepository.existsByUserIdAndClubId(
                    waitingListDto.getUserId(),
                    waitingListDto.getClubId()
            );
            if (exists) return null;

            // 신청 정보 저장
            WaitingList waiting = new WaitingList();
            waiting.setUserId(waitingListDto.getUserId());
            waiting.setClubId(waitingListDto.getClubId());
            waiting.setWhy(waitingListDto.getWhy());
            waiting.setWhat(waitingListDto.getWhat());
            waitingListRepository.save(waiting);

            ClubIdRequestDto clubIdRequestDto = new ClubIdRequestDto();
            clubIdRequestDto.setClubId(waitingListDto.getClubId());
            return clubIdRequestDto;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ClubIdRequestDto processClubApplication(WaitingListDecisionDto dto) {
        try {
            Optional<WaitingList> optional = waitingListRepository.findByUserIdAndClubId(dto.getUserId(), dto.getClubId());
            if (optional.isEmpty()) {
                return null;
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

            // ✅ 승인 또는 거절 후 clubId 반환
            ClubIdRequestDto result = new ClubIdRequestDto();
            result.setClubId(dto.getClubId());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

        // ✅ 인원 수 계산: 회장 + 임원 + 멤버
        int presidentCount = presidentRepository.findByClubId(clubId).isPresent() ? 1 : 0;
        int executiveCount = executiveRepository.countByClubId(clubId);
        int memberCount = clubMembersRepository.countByClubId(clubId);
        int totalCount = presidentCount + executiveCount + memberCount;

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

    public List<User> getAllUsersInClub(String clubId) {
        Set<String> seenUserIds = new HashSet<>();
        List<User> allUsers = new ArrayList<>();

        // 1. 일반 멤버
        List<User> members = clubMembersRepository.findByClubId(clubId)
                .stream()
                .map(ClubMembers::getUser)
                .filter(Objects::nonNull)
                .toList();

        for (User user : members) {
            if (seenUserIds.add(user.getUserId())) {
                allUsers.add(user);
            }
        }

        // 2. 임원
        List<User> executives = executiveRepository.findByClubId(clubId)
                .stream()
                .map(Executive::getUser)
                .filter(Objects::nonNull)
                .toList();

        for (User user : executives) {
            if (seenUserIds.add(user.getUserId())) {
                allUsers.add(user);
            }
        }

        // 3. 회장
        Optional<User> presidentOpt = presidentRepository.findByClubId(clubId)
                .map(President::getUser);

        if (presidentOpt.isPresent()) {
            User president = presidentOpt.get();
            if (seenUserIds.add(president.getUserId())) {
                allUsers.add(president);
            }
        }

        return allUsers;
    }


    /**
     * ✔ 동아리 생성 시 초기 회장 등록 (managerId를 기준으로)
     */
    public void registerInitialPresident(String clubId, String userId) {
        President president = new President();
        president.setClubId(clubId);
        president.setUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 userId의 유저가 존재하지 않습니다."));
        president.setUser(user);

        presidentRepository.save(president);
    }

    /**
     * ✔ 회장 위임 기능 (기존 회장은 덮어씀)
     */
    @Transactional
    public ClubDelegateDto delegatePresident(ClubDelegateDto clubDelegateDto) {
        String clubId = clubDelegateDto.getClubId();
        String newUserId = clubDelegateDto.getNewUserId();

        // 1. 기존 회장이 있으면 → 멤버로 이동
        Optional<President> optional = presidentRepository.findByClubId(clubId);

        optional.ifPresent(oldPresident -> {
            String oldUserId = oldPresident.getUserId();

            // 이전 회장과 newUser가 같으면 아무것도 안 해도 됨
            if (!oldUserId.equals(newUserId)) {
                boolean alreadyMember = clubMembersRepository.existsByUserIdAndClubId(oldUserId, clubId);
                if (!alreadyMember) {
                    ClubMembers downgraded = new ClubMembers();
                    downgraded.setUserId(oldUserId);
                    downgraded.setClubId(clubId);
                    downgraded.setRole(null);
                    clubMembersRepository.save(downgraded);
                }
            }
        });

        // 2. newUserId가 ClubMembers 또는 Executive에 있으면 제거
        ClubMembersId newId = new ClubMembersId(newUserId, clubId);
        clubMembersRepository.findById(newId).ifPresent(clubMembersRepository::delete);
        executiveRepository.findById(newId).ifPresent(executiveRepository::delete);

        // 3. 회장 교체 (기존 없으면 새로 생성)
        President president = optional.orElseGet(() -> {
            President p = new President();
            p.setClubId(clubId);
            return p;
        });

        president.setUserId(newUserId);

        User user = userRepository.findById(newUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음"));
        president.setUser(user);

        presidentRepository.save(president);

        return clubDelegateDto;
    }

    public boolean canManageClub(String userId, String clubId) {
        // 회장인지 확인
        boolean isPresident = presidentRepository.findByClubId(clubId)
                .map(p -> p.getUserId().equals(userId))
                .orElse(false);

        // 임원인지 확인
        boolean isExecutive = executiveRepository.existsByUserIdAndClubId(userId, clubId);

        return isPresident || isExecutive;
    }
}
